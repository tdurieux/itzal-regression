#!/usr/bin/env python

import requests

from ProgressBar import ProgressBar
from src.main.python.regression import get_workload
from mayocat import available_requests

DIRECT_HOST = "http://172.17.0.2:8080/"

workload = get_workload(available_requests, nb_session=10000)

nb_request = 0
for session in workload:
    nb_request += len(session)

progress = ProgressBar(nb_request, fmt=ProgressBar.FULL)
id_request = 0
for session in workload:
    s = requests.Session()
    csrf_token = None
    for req in session:
        response = None
        if req["method"] == "get":
            response = s.get(DIRECT_HOST + req["path"], verify=False, allow_redirects=False)
        elif req["method"] == "post":
            if "format" in req and req["format"] == "json":
                response = s.post(DIRECT_HOST + req["path"], json=req["data"], allow_redirects=False)
            else:
                response = s.post(DIRECT_HOST + req["path"], data=req["data"], allow_redirects=False)
        id_request += 1
        progress.current += 1
        progress()
progress.done()
