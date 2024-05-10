import os
import json
import requests
import urllib3

from dotenv import load_dotenv

urllib3.disable_warnings()

DEVICE_PASSWORD = "my-secret-password"
ENV_FILE_PATH = "../.env"

load_dotenv()

DEVICE_REGISTRY = os.getenv('DEVICE_REGISTRY')
registry_base_url = f"https://{DEVICE_REGISTRY}/v1"

tenant_id = os.getenv('TENANT_ID')
device_id = os.getenv('DEVICE_ID')

if tenant_id == None:
    # Register Tenant
    tenant = requests.post(
        url=f"{registry_base_url}/tenants",
        headers={"content-type": "application/json"},
        data=json.dumps({"ext": {"messaging-type": "amqp"}}), verify=False).json()

    tenant_id = tenant["id"]

    with open(ENV_FILE_PATH, "a") as env_file:
        env_file.write(f"TENANT_ID={tenant_id}\n")

    print(f"Registered tenant {tenant_id}")
else:
    print(f"Using tenant {tenant_id}")

if device_id == None:
    # Add Device to Tenant
    device = requests.post(f"{registry_base_url}/devices/{tenant_id}", verify=False).json()

    device_id = device["id"]

    with open(ENV_FILE_PATH, "a") as env_file:
        env_file.write(f"DEVICE_ID={device_id}\n")
        env_file.write(f"DEVICE_PASSWORD={DEVICE_PASSWORD}\n")

    print(f'Registered device {device_id}')

    # Set Device Password
    code = requests.put(
        url=f"{registry_base_url}/credentials/{tenant_id}/{device_id}",
        headers={"content-type": "application/json"},
        data=json.dumps(
            [{"type": "hashed-password", "auth-id": device_id, "secrets": [{"pwd-plain": DEVICE_PASSWORD}]}]), verify=False)

    if code.status_code == 204:
        print("Password is set!")
    else:
        print("Unable to set Password")
else:
    print(f"Existing device {device_id}")
