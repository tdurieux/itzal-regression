#!/usr/bin/env python

import requests
import os
from os import listdir
from os.path import isfile, join
import subprocess
import signal
import time
import shutil
import random
import json

from __builtin__ import range, len, str

from ProgressBar import ProgressBar

PATH_OUTPUT = "/home/thomas/git/itzal-regression"
STARTING_DATE = int(round(time.time() * 1000))


def workload(host, available_requests, path_destination, location, id_patch):
    nb_request = 0
    for session in available_requests:
        nb_request += len(session)

    progress = ProgressBar(nb_request, fmt=ProgressBar.FULL)

    id_request = 0
    for session in available_requests:
        s = requests.Session()
        csrf_token = None
        for req in session:
            response = None
            if req["method"] == "get":
                response = s.get(host + req["path"], verify=False, allow_redirects=False)
                if response.content is not None and "csrfToken" in response.content:
                    index = response.content.index("csrfToken")
                    csrf_token = response.content[index + 18:index + 57]
            elif req["method"] == "post":
                if csrf_token is not None:
                    req["data"]["csrfToken"] = csrf_token
                if "format" in req and req["format"] == "json":
                    response = s.post(host + req["path"], json=req["data"], allow_redirects=False)
                else:
                    response = s.post(host + req["path"], data=req["data"], allow_redirects=False)
            copy_output(path_destination, location, id_patch, id_request + 1)
            id_request += 1
            progress.current += 1
            progress()
    progress.done()


def copy_output(path_destination, location, patch_id, request_id):
    working_dir = os.getcwd()
    output_files = [f for f in listdir(working_dir) if "request-" in f and isfile(join(working_dir, f))]
    while len(output_files) == 0:
        time.sleep(0.1)
        output_files = [f for f in listdir(working_dir) if "request-" in f and isfile(join(working_dir, f))]
    for output in output_files:
        try:
            data = json.load(open(join(working_dir, output)))
        except ValueError:
            time.sleep(0.1)
            return copy_output(path_destination, location, patch_id, request_id)
        dest = path_destination
        if patch_id == 0:
            dir = join(dest, "original", str(STARTING_DATE))
        else:
            dir = join(dest, location[location.rfind(".") + 1:].replace(":", "-") + "-" + str(patch_id), str(STARTING_DATE))
        if not os.path.exists(dir):
            os.makedirs(dir)
        dest = join(dir, "%d.json" % request_id)
        shutil.move(join(working_dir, output), dest)
    pass


def run(location, id_patch, path_application):
    cmd = "java -jar %s/target/itzal-regression-0.1-SNAPSHOT-jar-with-dependencies.jar %s %s %s %d" % (
        PATH_OUTPUT,
        path_application,
        "http://localhost:8080",
        location,
        id_patch
    )
    # , stdout=subprocess.PIPE
    pro = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, preexec_fn=os.setsid)
    time.sleep(2)
    return pro


def kill(pro):
    os.killpg(os.getpgid(pro.pid), signal.SIGTERM)
    time.sleep(5)

random.seed(5)


def get_workload(available_requests, nb_session=25, min_req=3, max_req=7):
    requests = []

    for _ in range(0, nb_session):
        session = []
        for _ in range(0, random.randint(min_req, max_req)):
            id_request = random.randint(0, len(available_requests) - 1)
            session += [available_requests[id_request]]
        requests += [session]
    return requests


def start(patch_locations, available_requests, host, path_destination, path_application):
    # run original application
    requests = get_workload(available_requests)

    print "run original"
    pro = run("original:0", 0, path_application)
    try:
        workload(host, requests, path_destination, "", 0)
    finally:
        kill(pro)

    for location, nbPatch in patch_locations:
        for id_patch in range(1, nbPatch + 1):
            print location, id_patch
            pro = run(location, id_patch, path_application)
            try:
                workload(host, requests, path_destination, location, id_patch)
            finally:
                kill(pro)
