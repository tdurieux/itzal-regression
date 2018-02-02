#!/usr/bin/env python

from src.main.python.regression import start

HOST = "http://localhost:9999/"
LOCATIONS = [
    ("org.broadleafcommerce.core.catalog.service.RelatedProductsServiceImpl:208", 3),
    ("org.broadleafcommerce.core.order.domain.OrderItemImpl:418", 3),
    #("org.broadleafcommerce.core.search.service.solr.SolrHelperServiceImpl$1:593", 5),
    ("org.broadleafcommerce.core.search.service.solr.SolrHelperServiceImpl:531", 5),
    #("org.broadleafcommerce.core.catalog.domain.CategoryImpl:808", 7),
    ("org.broadleafcommerce.core.catalog.domain.CategoryImpl:835", 7)
]
PATH_APPLICATION = "/home/thomas/git/BroadleafCommerce-regression/site/"
PATH_DESTINATION = "/mnt/secondary/npefix-output/regression/broadleaf"


available_requests = [
    {
        "method": "get",
        "path": "",
    },
    {
        "method": "get",
        "path": "hot-sauces/hoppin_hot_sauce",
    },
    {
        "method": "get",
        "path": "hot-sauces",
    },
    {
        "method": "get",
        "path": "merchandise",
    },
    {
        "method": "get",
        "path": "mens",
    },
    {
        "method": "get",
        "path": "search?q=test",
    },
    {
        "method": "get",
        "path": "search?q=hot",
    },
    {
        "method": "get",
        "path": "search?q=hot&price=range[0.00000%3A5.00000]",
    },
    {
        "method": "get",
        "path": "clearance",
    },
    {
        "method": "get",
        "path": "new-to-hot-sauce",
    },
    {
        "method": "get",
        "path": "faq",
    },
    {
        "method": "get",
        "path": "?blLocaleCode=fr_FR",
    },
    {
        "method": "get",
        "path": "login",
    },
    {
        "method": "get",
        "path": "register",
    },
    {
        "method": "post",
        "path": "cart/add",
        "data": {
            "productId": 3,
            "quantity": 1
        }
    },
    {
        "method": "post",
        "path": "cart/add",
        "data": {
            "productId": 4,
            "quantity": 3
        }
    },
    {
        "method": "get",
        "path": "cart"
    }
]


start(patch_locations=LOCATIONS,
      available_requests=available_requests,
      host=HOST,
      path_destination=PATH_DESTINATION,
      path_application=PATH_APPLICATION)
