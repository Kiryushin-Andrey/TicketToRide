package redisClient;

import java.io.*
import java.net.Socket
import java.util.*

/**
 * A lightweight implementation of the Redis server protocol at https://redis.io/topics/protocol
 *
 *
 * Effectively a complete Redis client implementation.
 */
class Redis(inputStream: InputStream, outputStream: OutputStream) {
    /**
     * Implements the encoding (writing) side.
     */
    internal class Encoder
    /**
     * Construct the encoder with the passed outputstream the encoder will write to.
     *
     * @param out Will be used to write all encoded data to.
     */(
        /**
         * This stream we will write to.
         */
        private val out: OutputStream
    ) {

        /**
         * Write a byte array in the "RESP Bulk String" format.
         *
         * @param value The byte array to write.
         * @throws IOException Propagated from the output stream.
         * @link https://redis.io/topics/protocol#resp-bulk-strings
         */
        fun write(value: ByteArray) {
            out.write('$'.toInt())
            out.write(java.lang.Long.toString(value.size.toLong()).toByteArray())
            out.write(CRLF)
            out.write(value)
            out.write(CRLF)
        }

        /**
         * Write a long value in the "RESP Integers" format.
         *
         * @param `val` The value to write.
         * @throws IOException Propagated from the output stream.
         * @link https://redis.io/topics/protocol#resp-integers
         */
        fun write(value: Long) {
            out.write(':'.toInt())
            out.write(value.toString().toByteArray())
            out.write(CRLF)
        }

        /**
         * Write a list of objects in the "RESP Arrays" format.
         *
         * @param list A list of objects that contains Strings, Longs, Integers and (recursively) Lists.
         * @throws IOException              Propagated from the output stream.
         * @throws IllegalArgumentException If the list contains unencodable objects.
         * @link https://redis.io/topics/protocol#resp-arrays
         */
        fun write(list: List<*>) {
            out.write('*'.toInt())
            out.write(list.size.toLong().toString().toByteArray())
            out.write(CRLF)
            for (o in list) {
                when (o) {
                    is ByteArray -> write(o)
                    is String -> write(o.toByteArray())
                    is Long -> write(o)
                    is Int -> write(o.toLong())
                    is List<*> -> write(o)
                    else ->
                        throw IllegalArgumentException("Unexpected type " + o?.javaClass?.canonicalName)
                }
            }
        }

        fun flush() {
            out.flush()
        }

        companion object {
            /**
             * CRLF is used a lot.
             */
            private val CRLF = byteArrayOf('\r'.toByte(), '\n'.toByte())
        }

    }

    /**
     * Implements the parser (reader) side of protocol.
     */
    internal class Parser
    /**
     * Constructor.
     *
     * @param input The stream to read the data from.
     */(
        /**
         * The input stream used to read the data from.
         */
        private val input: InputStream
    ) {
        /**
         * Thrown whenever data could not be parsed.
         */
        internal class ProtocolException(msg: String?) : IOException(msg)

        /**
         * Thrown whenever an error string is decoded.
         */
        internal class ServerError(msg: String?) : IOException(msg)

        /**
         * Parse incoming data from the stream.
         *
         *
         * Based on each of the markers which will identify the type of data being sent, the parsing
         * is delegated to the type-specific methods.
         *
         * @return The parsed object
         * @throws IOException       Propagated from the stream
         * @throws ProtocolException In case unexpected bytes are encountered.
         */
        fun parse(): Any? {
            val ret: Any?
            val read = input.read()
            ret = when (read) {
                '+'.toInt() -> parseSimpleString()
                '-'.toInt() -> throw ServerError(String(parseSimpleString()))
                ':'.toInt() -> parseNumber()
                '$'.toInt() -> parseBulkString()
                '*'.toInt() -> {
                    val len = parseNumber()
                    if (len == -1L) {
                        null
                    } else {
                        val arr: MutableList<Any?> = LinkedList()
                        var i: Long = 0
                        while (i < len) {
                            arr.add(parse())
                            i++
                        }
                        arr
                    }
                }
                -1 -> return null
                else -> throw ProtocolException("Unexpected input: " + read.toByte())
            }
            return ret
        }

        /**
         * Parse "RESP Bulk string" as a String object.
         *
         * @return The parsed response
         * @throws IOException Propagated from underlying stream.
         */
        private fun parseBulkString(): ByteArray? {
            val expectedLength = parseNumber()
            if (expectedLength == -1L) {
                return null
            }
            if (expectedLength > Int.MAX_VALUE) {
                throw ProtocolException("Unsupported value length for bulk string")
            }
            val numBytes = expectedLength.toInt()
            val buffer = ByteArray(numBytes)
            var read = 0
            while (read < expectedLength) {
                read += input.read(buffer, read, numBytes - read)
            }
            if (input.read() != '\r'.toInt()) {
                throw ProtocolException("Expected CR")
            }
            if (input.read() != '\n'.toInt()) {
                throw ProtocolException("Expected LF")
            }
            return buffer
        }

        /**
         * Parse "RESP Simple String"
         *
         * @return Resultant string
         * @throws IOException Propagated from underlying stream.
         */
        private fun parseSimpleString(): ByteArray {
            return scanCr(1024)
        }

        private fun parseNumber(): Long {
            return java.lang.Long.valueOf(String(scanCr(1024)))
        }

        private fun scanCr(size: Int): ByteArray {
            var size = size
            var idx = 0
            var ch: Int
            var buffer = ByteArray(size)
            while (input.read().also { ch = it } != '\r'.toInt()) {
                buffer[idx++] = ch.toByte()
                if (idx == size) {
                    // increase buffer size.
                    size *= 2
                    buffer = Arrays.copyOf(buffer, size)
                }
            }
            if (input.read() != '\n'.toInt()) {
                throw ProtocolException("Expected LF")
            }
            return Arrays.copyOfRange(buffer, 0, idx)
        }

    }

    /**
     * Used for writing the data to the server.
     */
    private val writer: Encoder

    /**
     * Used for reading responses from the server.
     */
    private val reader: Parser
    /**
     * Construct the connection with the specified Socket as the server connection with specified buffer sizes.
     *
     * @param socket           Socket to connect to
     * @param inputBufferSize  buffer size in bytes for the input stream
     * @param outputBufferSize buffer size in bytes for the output stream
     * @throws IOException If a socket error occurs.
     */
    /**
     * Construct the connection with the specified Socket as the server connection with default buffer sizes.
     *
     * @param socket Connected socket to the server.
     * @throws IOException If a socket error occurs.
     */
    @JvmOverloads
    constructor(socket: Socket, inputBufferSize: Int = 1 shl 16, outputBufferSize: Int = 1 shl 16) : this(
        BufferedInputStream(socket.getInputStream(), inputBufferSize),
        BufferedOutputStream(socket.getOutputStream(), outputBufferSize)
    ) {
    }

    /**
     * Execute a Redis command and return it's result.
     *
     * @param args Command and arguments to pass into redis.
     * @param <T>  The expected result type
     * @return Result of redis.
     * @throws IOException All protocol and io errors are IO exceptions.
    </T> */
    fun <T> call(vararg args: Any?): T? {
        writer.write(Arrays.asList(*args as Array<Any?>))
        writer.flush()
        return read()
    }

    /**
     * Does a blocking read to wait for redis to send data.
     *
     * @param <T> The expected result type.
     * @return Result of redis
     * @throws IOException Propagated
    </T> */
    fun <T> read(): T? {
        return reader.parse() as T?
    }

    /**
     * Helper class for pipelining.
     */
    abstract class Pipeline {
        /**
         * Write a new command to the server.
         *
         * @param args Command and arguments.
         * @return self for chaining
         * @throws IOException Propagated from underlying server.
         */
        abstract fun call(vararg args: String?): Pipeline?

        /**
         * Returns an aligned list of responses for each of the calls.
         *
         * @return The responses
         * @throws IOException Propagated from underlying server.
         */
        abstract fun read(): kotlin.collections.List<Any?>?
    }

    /**
     * Create a pipeline which writes all commands to the server and only starts
     * reading the response when read() is called.
     *
     * @return A pipeline object.
     */
    fun pipeline(): Pipeline {
        return object : Pipeline() {
            private var n = 0

            override fun call(vararg args: String?): Pipeline {
                writer.write(Arrays.asList(*args as Array<Any?>))
                writer.flush()
                n++
                return this
            }

            override fun read(): kotlin.collections.List<Any?> {
                val ret: MutableList<Any?> = LinkedList()
                while (n-- > 0) {
                    ret.add(reader.parse())
                }
                return ret
            }
        }
    }

    @FunctionalInterface
    interface FailableConsumer<T, E : Throwable?> {
        fun accept(t: T)
    }

    companion object {
        /**
         * Utility method to execute some command with redis and close the connection directly after.
         *
         * @param callback The callback to perform with redis.
         * @param addr     Connection IP address
         * @param port     Connection port
         * @throws IOException Propagated
         */
        fun run(callback: FailableConsumer<Redis?, IOException?>, addr: String?, port: Int) {
            Socket(addr, port).use { s -> run(callback, s) }
        }

        fun run(callback: FailableConsumer<Redis?, IOException?>, s: Socket) {
            callback.accept(Redis(s))
        }
    }

    /**
     * Construct with the specified streams to respectively read from and write to.
     *
     * @param inputStream  Read from this stream
     * @param outputStream Write to this stream
     */
    init {
        reader = Parser(inputStream)
        writer = Encoder(outputStream)
    }
}