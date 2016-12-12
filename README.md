Search Engine
=============
I wrote this as part of my graduate studies at UC Irvine.  In a previous step, I crawled the
ics.uci.edu website and collected the postings in a Mongo DB database.  In this step, I create
index the postings and create a query engine to search the results.

Indexer
-------
I've been wanting to learn Akka for a while and this was a perfect opportunity.  I was blown away
that with Akka I was able to index over 50,000 pages a minute.


QueryEngine
-----------
My initial implementation was trivial.  Document scores were computed by adding
the **tf-idf** scores for each term in the document that appears in the query.
I then implemented **cosine similarity** and added a **two-gram index**.  I
tried all the different weightings of **tf-idf**, but on average, the basic
formula gave the best results.

For evaluation, in addition to **NDCG**, I used a modified version of
**f-measure** to help measure the results.  For a query result of 10, it
considers anything in that appears in the top 40 Google results to be
"relevant".

UI
---
The UI is built using the Play! Framework.  It uses the Google Custom Search API to display
my results against Google's for comparison.
