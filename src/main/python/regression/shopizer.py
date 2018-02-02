#!/usr/bin/env python

from src.main.python.regression import start

HOST = "http://localhost:9999/"
LOCATIONS = [
    ("com.salesmanager.shop.store.controller.category.facade.CategoryFacadeImpl:55", 5),
    ("com.salesmanager.shop.populator.catalog.ReadableCategoryPopulator:51", 5),
    ("com.salesmanager.shop.populator.catalog.ReadableProductPopulator:94", 4),
    ("com.salesmanager.shop.store.controller.category.ShoppingCategoryController:253", 8)
]
PATH_APPLICATION = "/home/thomas/git/shopizer-regression/sm-shop/"
PATH_DESTINATION = "/mnt/secondary/npefix-output/regression/shopizer"

available_requests = [
    {
        "method": "get",
        "path": "shop",
    },
    {
        "method": "get",
        "path": "shop/product/the-big-switch.html",
    },
    {
        "method": "get",
        "path": "shop/cart/shoppingCart.html",
    },
    {
        "method": "get",
        "path": "shop/product/Spring-in-Action.html",
    },
    {
        "method": "get",
        "path": "shop/cart/shoppingCart.html",
    },
    {
        "method": "get",
        "path": "shop/product/Node-Web-Development.html",
    },
    {
        "method": "get",
        "path": "shop/cart/shoppingCart.html",
    },
    {
        "method": "get",
        "path": "shop/category/business.html/ref=c:6",
    },
    {
        "method": "get",
        "path": "shop/category/computer-books.html/ref=c:6,1",
    },
    {
        "method": "post",
        "path": "shop/cart/addShoppingCartItem",
        "data": {
            "quantity": 1,
            "productId": 6
        },
        "format": "json"
    },
    {
        "method": "post",
        "path": "shop/cart/addShoppingCartItem",
        "data": {
            "quantity": 1,
            "productId": 2
        },
        "format": "json"
    },
    {
        "method": "post",
        "path": "shop/cart/addShoppingCartItem",
        "data": {
            "quantity": 2,
            "productId": 3
        },
        "format": "json"
    }

]


start(patch_locations=LOCATIONS,
      available_requests=available_requests,
      host=HOST,
      path_destination=PATH_DESTINATION,
      path_application=PATH_APPLICATION)
