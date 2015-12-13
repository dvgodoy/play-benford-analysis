# Play Benford Analysis

Play Application and RESTful APIs for the ***Benford Analysis for Spark*** package, which contains detection methods based on Benford's Law.

Although traditionally known as a tool for accounting audits, Benford's Law wide range of applications also include image and signal processing and social network analysis. Learn more about Benford's Law, its applications and how Benford Analysis implements its methods in [Benford Analysis for Spark](https://github.com/dvgodoy/spark-benford-analysis) GitHub repo.

## API Methods

### Image Processing

/api/img/Direct

/api/img/URL

/api/:job/SBA/:wSize

/api/:job/Image

/api/:job/NewImage/:perc/:white

### Accounting

/api/acc/Upload

/api/acc/URL

/api/:job/Groups

/api/:job/Calc/:samples

/api/:job/FreqByGroup/:group

/api/:job/TestsByGroup/:group

/api/:job/CIsByGroup/:group

/api/:job/BenfCIsByGroup/:group

/api/:job/ResultsByGroup/:group
