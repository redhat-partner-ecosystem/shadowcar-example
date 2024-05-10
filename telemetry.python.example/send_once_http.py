import os
import sys
import json
import time
import requests
import urllib3
from requests.auth import HTTPBasicAuth
from dotenv import load_dotenv

urllib3.disable_warnings()

ENV_FILE_PATH = "../.env"

load_dotenv()

DEVICE_PASSWORD = os.getenv('DEVICE_PASSWORD')
ADAPTER_HTTPS = os.getenv('ADAPTER_HTTPS')

adapter_https_url = f"https://{ADAPTER_HTTPS}"
tenant_id = os.getenv('TENANT_ID')
device_id = os.getenv('DEVICE_ID')


def send_message_via_http_adapter():
    return requests.post(
        url=f"{adapter_https_url}/telemetry",
        headers={"content-type": "application/json"},
        data=json.dumps({"temp": 5, "transport": "http"}),
        auth=HTTPBasicAuth(f"{device_id}@{tenant_id}", DEVICE_PASSWORD),
        verify=False)


if tenant_id == None or device_id == None:
    print("Missing TENANT_ID or DEVICE_ID. Run setup.py first.")
    sys.exit()

# Send HTTP Message
print("Sending Telemetry message via HTTP adapter")
response = send_message_via_http_adapter()
while response.status_code != 202:
    print(f"failed to send message via HTTP adapter, status code: {response.status_code}")
    time.sleep(2)
    print("trying again ...")
    response = send_message_via_http_adapter()
