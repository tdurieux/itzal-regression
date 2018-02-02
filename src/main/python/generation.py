#!/usr/bin/env python

import os
from os import listdir
from os.path import isfile, join
import subprocess
import shutil

PATH_DESTINATION = "/mnt/secondary/npefix-output/bugs/"

bugs = [
    {
        "simple_name": "DataflowJavaSDK_c06125d",
        "path": "/home/thomas/git/DataflowJavaSDK",
        "type": "mvn"
    },
    {
        "simple_name": "math_988b",
        "path": "/home/thomas/git/npedataset/math-988b",
        "type": "mvn"
    },
    {
        "simple_name": "jetty_335500",
        "path": "/mnt/secondary/npe-dataset/jetty-335500",
        "type": "workload"
    },
    {
        "simple_name": "tomcat_58232",
        "path": "/mnt/secondary/npe-dataset/tomcat-58232",
        "type": "workload"
    },
    {
        "simple_name": "math_1115",
        "path": "/home/thomas/git/npedataset/math-1115",
        "type": "mvn"
    },
    {
        "simple_name": "math_988a",
        "path": "/home/thomas/git/npedataset/math-988a",
        "type": "mvn"
    },
    {
        "simple_name": "lang_703",
        "path": "/home/thomas/git/npedataset/lang-703",
        "type": "mvn"
    },
    {
        "simple_name": "tomcat_54703",
        "path": "/mnt/secondary/npe-dataset/tomcat-54703",
        "type": "workload"
    },
    {
        "simple_name": "math_79",
        "path": "/mnt/secondary/projects/math/math_79",
        "type": "mvn"
    },
    {
        "simple_name": "pdfbox_2995",
        "path": "/home/thomas/git/npedataset/pdfbox_2995",
        "type": "mvn"
    },
    {
        "simple_name": "collections_360",
        "path": "/home/thomas/git/npedataset/collections-360",
        "type": "mvn"
    },
    {
        "simple_name": "lang_33",
        "path": "/mnt/secondary/projects/lang/lang_33",
        "type": "mvn"
    },
    {
        "simple_name": "tomcat_56010",
        "path": "/mnt/secondary/npe-dataset/tomcat-56010",
        "type": "workload"
    },
    {
        "simple_name": "math_369",
        "path": "/home/thomas/git/npedataset/math-369",
        "type": "mvn"
    },
    {
        "simple_name": "lang_20",
        "path": "/mnt/secondary/projects/lang/lang_20",
        "type": "mvn"
    },
    {
        "simple_name": "webmagic_ff2f588",
        "path": "/home/thomas/git/webmagic",
        "type": "mvn"
    },
    {
        "simple_name": "math_1117",
        "path": "/home/thomas/git/npedataset/math-1117",
        "type": "mvn"
    },
    {
        "simple_name": "javapoet_70b38e5",
        "path": "/home/thomas/git/javapoet",
        "type": "mvn"
    },
    {
        "simple_name": "pdfbox_2965",
        "path": "/home/thomas/git/npedataset/pdfbox_2965",
        "type": "mvn"
    },
    {
        "simple_name": "math_70",
        "path": "/mnt/secondary/projects/math/math_70",
        "type": "mvn"
    },
    {
        "simple_name": "tomcat_55454",
        "path": "/mnt/secondary/npe-dataset/tomcat-55454",
        "type": "workload"
    },
    {
        "simple_name": "tomcat_43758",
        "path": "/mnt/secondary/npe-dataset/tomcat-43758",
        "type": "workload"
    },
    {
        "simple_name": "sling_4982",
        "path": "/home/thomas/git/npedataset/sling_4982",
        "type": "mvn"
    },
    {
        "simple_name": "math_305",
        "path": "/home/thomas/git/npedataset/math-305",
        "type": "mvn"
    },
    {
        "simple_name": "math_290",
        "path": "/home/thomas/git/npedataset/math-290",
        "type": "mvn"
    },
    {
        "simple_name": "math_4",
        "path": "/mnt/secondary/projects/math/math_4",
        "type": "mvn"
    },
    {
        "simple_name": "felix_4960",
        "path": "/home/thomas/git/npedataset/felix-4960",
        "type": "mvn"
    },
    {
        "simple_name": "jongo_f46f658",
        "path": "/home/thomas/git/jongo",
        "type": "mvn"
    },
    {
        "simple_name": "lang_304",
        "path": "/home/thomas/git/npedataset/lang-304",
        "type": "mvn"
    },
    {
        "simple_name": "lang_39",
        "path": "/mnt/secondary/projects/lang/lang_39",
        "type": "mvn"
    },
    {
        "simple_name": "mayocat_231",
        "path": "/home/thomas/git/npedataset/collections-360",
        "type": "shadow"
    },
    {
        "simple_name": "pdfbox_2812",
        "path": "/home/thomas/git/npedataset/pdfbox_2812",
        "type": "mvn"
    },
    {
        "simple_name": "broadleaf_1282",
        "path": "/home/thomas/git/npedataset/collections-360",
        "type": "shadow"
    },
    {
        "simple_name": "lang_587",
        "path": "/home/thomas/git/npedataset/lang-587",
        "type": "mvn"
    }
]


def copyResults(path, bug, tool):
    working_dir = join(path, "target", "npefix")
    print working_dir
    output_files = [f for f in listdir(working_dir) if "patches_" in f and isfile(join(working_dir, f))]
    for output in output_files:
        dest = PATH_DESTINATION
        dir = join(dest, bug, tool)
        if not os.path.exists(dir):
            os.makedirs(dir)
        dest = join(dir, output.replace("patches_", ""))
        shutil.move(join(working_dir, output), dest)



def npefix(bug):
    cmd = "cd %s; mvn fr.inria.gforge.spirals:npefix-maven:1.4-SNAPSHOT:npefix -Dlaps=4000 -Dscope=stack" % (bug["path"])
    subprocess.call(cmd, shell=True)
    copyResults(bug["path"], bug["simple_name"], "npefix")


def trycatch(bug):
    cmd = "cd %s; mvn fr.inria.gforge.spirals:npefix-maven:1.4-SNAPSHOT:npefix -Dlaps=4000 -Dscope=stack -Dstrategy=TryCatch" % (bug["path"])
    subprocess.call(cmd, shell=True)
    copyResults(bug["path"], bug["simple_name"], "trycatch")

for bug in bugs:
    if bug["type"] == "mvn":
        print bug["simple_name"]
        npefix(bug)

