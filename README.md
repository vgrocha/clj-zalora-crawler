# zalora-crawler

This is a small single threaded crawler to scrap all the pages on http://zalora.sg and return the first 20 items (SKU) and some statistics on the page. The crawler starts at the main page and crawls sites with the following subpath:

http://zalora.sg/([a-zA-Z0-9\-_])+{3}

Which basically means, subpath with 3 level, e.g.

http://zalora.sg/level1/level2/level3/

which corresponds to the main categories of the website.

The scrapper stores the files to process in a queue and the visited ones in a set (so we don't repeat ourselves).

For debugging purposes, it spits into "tovisit.txt" the list o url remaining to visit, while in "visited.txt" it spits the list of already visited sites, so we dont re-visit them.
The obtained SKUs (items) are in "skus.csv".

Statistics for the page are stored in "stats.csv", where you have the
min price of the page, max price, avg discount and the fraction of
products that have discount (so you know where the "fire sales" are
happening)

Also, it outputs "url-errors.txt". which are url's which had some problems to find

## Usage

    $ lein run

in the project root.


## License

Copyright © 2013 vgrocha

Distributed under the Beerware, the same as beer recipes.
