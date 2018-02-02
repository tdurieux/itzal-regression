#!/usr/bin/env python

from src.main.python.regression import start

HOST = "http://localhost:9999/"
LOCATIONS = [
    # ("org.mayocat.context.AbstractScopeCookieContainerFilter:202", 5),
    # ("org.mayocat.context.AbstractScopeCookieContainerFilter:256", 4),
    # ("org.mayocat.store.rdbms.dbi.argument.PostgresUUIDArrayArgumentFactory:30", 5),
    # ("org.mayocat.store.rdbms.dbi.argument.DateAsTimestampArgumentFactory:30", 5),
    # ("org.mayocat.store.rdbms.dbi.argument.MapAsJsonArgumentFactory:30", 5),
    ("org.mayocat.shop.shipping.strategy.FlatStrategyPriceCalculator:38", 7),
    # ("org.mayocat.shop.catalog.store.jdbi.mapper.ProductMapper:44", 5),
    # ("org.mayocat.theme.Theme:84", 3),
    # ("org.mayocat.shop.cart.internal.DefaultCartManager:198", 2),
    # ("org.mayocat.shop.cart.internal.DefaultCartLoader:88", 2)
]
PATH_APPLICATION = "/home/thomas/git/itzal-regression/projects/mayocat-shop-reression/shop/application"
PATH_DESTINATION = "/mnt/secondary/npefix-output/regression/mayocat"

available_requests = [
    {
        "method": "get",
        "path": "",
    },
    {
        "method": "get",
        "path": "products/",
    },
    {
        "method": "get",
        "path": "products/product",
    },
    {
        "method": "get",
        "path": "cart",
    },
    {
        "method": "post",
        "path": "cart/add",
        "data": {"product": "product"}
    },
    {
        "method": "post",
        "path": "cart/add",
        "data": {"product": "test"}
    },
    {
        "method": "get",
        "path": "products/test",
    },
    {
        "method": "get",
        "path": "news",
    },
    {
        "method": "get",
        "path": "pages/my-news",
    }
]
if __name__ == "__main__":
    start(patch_locations=LOCATIONS,
          available_requests=available_requests,
          host=HOST,
          path_destination=PATH_DESTINATION,
          path_application=PATH_APPLICATION)
