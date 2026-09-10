#!/usr/bin/env bash
# Environment setup for TicketToRide (Kotlin Multiplatform: JVM server + Kotlin/JS client).
# Runs in two modes, distinguished by AIR_STARTUP_MODE:
#   warmup - snapshot-baking run (also used by env-setup verification): blocks on healthcheck.
#   task   - real task run: starts the server in the background and returns promptly.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$REPO_ROOT"

if [ "${AIR_STARTUP_MODE:-}" = warmup ]; then WARMUP=1; else WARMUP=; fi

log() { printf '[startup] %s\n' "$*"; }

# --- JDK 17 (Gradle 7.6 / the Kotlin toolchain need it; the image only ships a newer JDK) ---
JDK_DIR="$HOME/.local/jdk17"
if [ ! -x "$JDK_DIR/bin/java" ] || ! "$JDK_DIR/bin/java" -version 2>&1 | grep -q '"17\.'; then
  log "Installing Temurin JDK 17..."
  rm -rf "$JDK_DIR" "$HOME/.local/jdk17.tar.gz"
  mkdir -p "$JDK_DIR"
  curl -fsSL -o "$HOME/.local/jdk17.tar.gz" \
    https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_linux_hotspot_17.0.13_11.tar.gz
  tar -xzf "$HOME/.local/jdk17.tar.gz" -C "$JDK_DIR" --strip-components=1
  rm -f "$HOME/.local/jdk17.tar.gz"
else
  log "JDK 17 already installed."
fi

# --- Persist env for future shells (login runs this script as a child; it does not inherit exports) ---
ENV_FILE="$HOME/.air-tickettoride-env.sh"
cat > "$ENV_FILE" <<EOF
export JAVA_HOME="$JDK_DIR"
export PATH="$JDK_DIR/bin:\$PATH"
EOF

PROFILE_FILE=""
for f in "$HOME/.bash_profile" "$HOME/.bash_login" "$HOME/.profile"; do
  if [ -f "$f" ]; then PROFILE_FILE="$f"; break; fi
done
[ -z "$PROFILE_FILE" ] && PROFILE_FILE="$HOME/.profile" && touch "$PROFILE_FILE"

MARKER="# tickettoride-env (managed by .air/cloud/startup.sh)"
for f in "$PROFILE_FILE" "$HOME/.bashrc"; do
  if [ -f "$f" ] && ! grep -qF "$MARKER" "$f"; then
    { echo "$MARKER"; echo "[ -f \"$ENV_FILE\" ] && . \"$ENV_FILE\""; } >> "$f"
  fi
done

export JAVA_HOME="$JDK_DIR"
export PATH="$JDK_DIR/bin:$PATH"

# --- Route the JVM through the egress proxy (the JVM ignores *_PROXY env vars, unlike curl/node/yarn) ---
if [ -n "${HTTPS_PROXY:-}" ]; then
  proxy_hostport="${HTTPS_PROXY#*://}"
  proxy_host="${proxy_hostport%%:*}"
  proxy_port="${proxy_hostport##*:}"
  export GRADLE_OPTS="-Dhttps.proxyHost=$proxy_host -Dhttps.proxyPort=$proxy_port -Dhttp.proxyHost=$proxy_host -Dhttp.proxyPort=$proxy_port ${GRADLE_OPTS:-}"
fi

chmod +x ./gradlew

# yarn (invoked internally by the Kotlin/JS plugin) hits packages pinned in
# kotlin-js-store/yarn.lock to registry.yarnpkg.com, and node's HTTPS client reliably
# aborts some of those downloads part-way through over this proxy (plain curl does not).
# Pre-fetch every pinned tarball with curl into a yarn offline mirror so `yarn install`
# reads them from disk instead of hitting the flaky path.
YARN_LOCK="$REPO_ROOT/kotlin-js-store/yarn.lock"
MIRROR_DIR="$HOME/.yarn-offline-mirror"
if [ -f "$YARN_LOCK" ]; then
  log "Pre-fetching npm packages pinned in yarn.lock into an offline mirror..."
  mkdir -p "$MIRROR_DIR"
  grep -oE 'resolved "[^"]+"' "$YARN_LOCK" | sed -E 's/^resolved "//; s/"$//' | sort -u > /tmp/tickettoride-yarn-urls.txt
  fetch_one() {
    local full="$1"
    local url="${full%%#*}"
    local sha1="${full#*#}"
    [ "$sha1" = "$full" ] && sha1=""
    local fname
    fname="$(echo "$url" | awk -F/ '{n=NF; f=$n; scope=$(n-3); if (scope ~ /^@/) print scope "-" f; else print f}')"
    local out="$MIRROR_DIR/$fname"
    if [ -s "$out" ] && { [ -z "$sha1" ] || [ "$(sha1sum "$out" | cut -d' ' -f1)" = "$sha1" ]; }; then
      return 0
    fi
    for _ in 1 2 3; do
      curl -fsSL --max-time 60 -o "$out" "$url" || continue
      if [ -z "$sha1" ] || [ "$(sha1sum "$out" | cut -d' ' -f1)" = "$sha1" ]; then
        return 0
      fi
    done
    rm -f "$out"
    echo "failed to fetch (or verify) $url" >&2
    return 1
  }
  export -f fetch_one
  export MIRROR_DIR
  xargs -P 8 -n 1 -I{} bash -c 'fetch_one "$@"' _ {} < /tmp/tickettoride-yarn-urls.txt || true
  rm -f /tmp/tickettoride-yarn-urls.txt

  cat > "$HOME/.yarnrc" <<EOF
yarn-offline-mirror "$MIRROR_DIR"
yarn-offline-mirror-pruning false
network-timeout 300000
EOF
fi

# --- Build: primes the Gradle distribution, Maven deps, Kotlin/JS Node+Yarn toolchain and npm
# packages, and compiles+bundles both the server jar and the client JS it serves. This is the
# expensive, cacheable part that the snapshot makes free for real tasks. ---
log "Building server (compiles Kotlin/JVM + Kotlin/JS, primes Gradle/npm caches)..."
attempt=1
until ./gradlew --no-daemon :server:build -x test; do
  if [ "$attempt" -ge 3 ]; then
    log "Build failed after $attempt attempts."
    exit 1
  fi
  log "Build attempt $attempt failed; retrying..."
  attempt=$((attempt + 1))
done
log "Build finished."

SERVER_JAR="$REPO_ROOT/server/build/libs/server-all.jar"
SERVER_LOG="/tmp/tickettoride-server.log"

start_server() {
  if pgrep -f "server-all.jar" > /dev/null 2>&1; then
    log "Server already running."
    return
  fi
  log "Starting server..."
  nohup java -jar "$SERVER_JAR" > "$SERVER_LOG" 2>&1 &
  disown
}

healthcheck() {
  log "Waiting for server to answer on port 8080..."
  local waited=0
  until curl -fsS -o /dev/null http://localhost:8080/ 2>/dev/null; do
    sleep 2
    waited=$((waited + 2))
    if [ $((waited % 20)) -eq 0 ]; then
      log "Still waiting for server (${waited}s)... last log lines:"
      tail -n 5 "$SERVER_LOG" 2>/dev/null || true
    fi
  done
  log "Server is up and responding."
}

start_server

if [ -n "$WARMUP" ]; then
  healthcheck
else
  log "Task mode: server starting in background, not waiting."
fi

log "Startup complete."
