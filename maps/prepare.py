import os
import requests
import shutil
import subprocess
from bs4 import BeautifulSoup

rootUrl = "https://download.geofabrik.de/"

def fetch_subregions(url, indent, parentFolder):
    response = requests.get(url)
    response.encoding = 'utf-8'
    soup = BeautifulSoup(response.text, "html.parser")

    for table in soup.findAll("table", id="subregions"):
        for row in table.findAll("tr"):
            nameLink = next((link for link in row.css.select('td.subregion > a') if link.get('href').endswith('.html')),
                            None)
            if nameLink != None:
                name = nameLink.find_all(string=True, recursive=True)[-1].get_text().strip('()')
                os.makedirs("{}/{}".format(parentFolder, name), exist_ok=True)

                pbfUrl = url.rsplit('/', 1)[0] + "/" + next(
                    (link.get('href') for link in row.css.select('td > a') if link.get('href').endswith('.pbf')), None)

                destination_path = "{}/{}.osm.pbf".format(parentFolder, name)
                old_destination_path = "{}/{}/map-filtered.osm.pbf".format(parentFolder, name)
                if os.path.exists(old_destination_path) and not os.path.exists(destination_path):
                    shutil.move(old_destination_path, destination_path)

                filtered_path = "{}/{}.filtered.osm.pbf".format(parentFolder, name)
                old_filtered_path = "{}/{}/map-filtered.osm.pbf".format(parentFolder, name)
                if os.path.exists(old_filtered_path) and not os.path.exists(filtered_path):
                    shutil.move(old_destination_path, filtered_path)

                result_path = "{}/{}.map".format(parentFolder, name)
                old_result_path = "{}/{}/generated.map".format(parentFolder, name)
                if os.path.exists(old_result_path) and not os.path.exists(result_path):
                    shutil.move(old_result_path, result_path)

                if not os.path.exists(destination_path) and not os.path.exists(filtered_path) and not os.path.exists(
                    result_path):
                    print("Downloading {}...".format(destination_path))
                    r = requests.get(pbfUrl, stream=True)
                    if r.status_code == 200:
                        with open(destination_path, 'wb') as f:
                            for chunk in r.iter_content(chunk_size=8192):
                                f.write(chunk)
                    else:
                        print("Error: Unable to fetch data from {}, status code {}".format(pbfUrl, r.status_code))

                if not os.path.exists(filtered_path) and not os.path.exists(result_path):
                    print("Filtering {}...".format(destination_path))
                    os.system(
                        "/usr/local/bin/osmium tags-filter \"{}\" railway=rail,halt,station,stop public_transport=stop_position route=railway,train,tracks -o \"{}\"".format(
                            destination_path, filtered_path))
                    if os.path.exists(filtered_path):
                        os.remove(destination_path)

                if not os.path.exists(result_path):
                    print("Generating {}...".format(result_path))
                    subprocess.call(
                        ["java", "-jar", "osm-tickettoride.jar", f"--in={filtered_path}", f"--out={result_path}"])

                folderUrl = rootUrl + nameLink.get('href')
                print(" " * indent + name + " " + folderUrl)
                fetch_subregions(folderUrl, indent + 1, "{}/{}/".format(parentFolder, name))

                def delete_subfolder_no_map(subfolder):
                    for root, dirs, files in os.walk(subfolder):
                        if any(file.endswith('.map') for file in files):
                            return
                    shutil.rmtree(subfolder)

                delete_subfolder_no_map("{}/{}".format(parentFolder, name))


fetch_subregions(rootUrl, 0, ".")
