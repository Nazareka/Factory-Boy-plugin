Adding support for Factory boy and django packages.
Now it's only adding autocomplete for instances created by factories.
It will be only works if the name of class of instance is equal to name of factory class without suffix(e.g. Job -> JobFactory)