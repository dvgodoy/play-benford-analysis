# Play Benford Analysis

Play Application and RESTful APIs for the ***Benford Analysis for Spark*** package, which contains detection methods based on Benford's Law.

Although traditionally known as a tool for accounting audits, Benford's Law wide range of applications also include image and signal processing and social network analysis. Learn more about Benford's Law, its applications and how Benford Analysis implements its methods in [Benford Analysis for Spark](https://github.com/dvgodoy/spark-benford-analysis) GitHub repo.

## API Methods

### Image Processing

#### Pipeline

1 - Submit an image using either `Direct` or `URL`;

2 - Use the `job` number in the response to make further API calls referring to the submitted image;

2 - Choose a window size (think of a convolutional kernel, you can use values as small as 3 for small images and/or images with sharp edges or as large as 25) and perform an SBA operation with `SBA`;

3 - Choose the percentage pixels with lowest SBA scores to be discarded (80% = 0.8 is usual, some images can keep its original features even with 95% of its pixels discarded) and the color of your background (black or white) and get the processed image as a Base64 string with `NewImage`;

4 - (optional) Get the original image as a Base64 string with `Image`.

#### Resources

***1 - Direct***

To submit directly an image file using a POST request. Please use ```imgData``` as parameter to submit it.

Endpoint:

    POST /api/img/Direct

Example Response:

    { 'job': '53fbd735-f787-444c-a701-f65f4887be2e' }

Attributes:

    job string -- The job ID to be used to process the image submitted.

***2 - URL***

To submit an image URL using a POST request. Please use ```imgURL``` as parameter to submit it.

Endpoint:

    POST /api/img/URL

Example Response:

    { 'job': '53fbd735-f787-444c-a701-f65f4887be2e' }

Attributes:

    job string -- The job ID to be used to process the image submitted.

***3 - Image***

To return the original image as a Base64 string.

Endpoint:

    GET /api/:job/Image

Example Response:

    { 'image': 'iVBORw0KGgoAAAANS... ' }

Attributes:

    image string -- Original image encoded as Base64 string.

***4 - SBA***

To perform a 2D-Scanning Benford Analysis (SBA) over an image using a given window size (think of a convolutional kernel, you can use values as small as 3 for small images and/or images with sharp edges or as large as 25 ).

Endpoint:

    GET /api/:job/SBA/:wSize

Example Response:

    { 'calc': 'ok' }

Attributes:

    calc string -- Returned status of the calculation.

***5 - NewImage***

To return a processed image as a Base64 string, given the percentage [0..1] of pixels with lowest scores at SBA to be discarded (80% = 0.8 is usual, some images can keep its original features even with 95% of its pixels discarded) and choose of a white background color (white = 1) instead of black.

Endpoint:

    /api/:job/NewImage/:perc/:white

Example Response:

    { 'image': 'iVBORw0KGgoAAAANS... ' }

Attributes:

    image string -- Processed image encoded as Base64 string.

### Accounting

#### Pipeline

1 - Submit a CSV file using either `Upload` or `URL`;

2 - Use the `job` number in the response to make further API calls referring to the submitted CSV file;

3 - Load the submitted file into Spark using `Load`;

4 - Choose the number of resamples in the bootstrap procedure (usually 25,000, but it can be set to 1,000 for learning purposes) and start calculation with 'Calc';

5 - (optional) If there is an hierarchical strucutre associated with the values, get generated group IDs and names using `Groups`.

If the file only contains a column of values, assume group ID equals 0.

6 - Get results for a given group ID using `ResultsByGroup`;

7 - (optional) Get observed frequencies in the CSV file for a given group with `FreqByGroup`;

8 - (optional) Get Z-Test and ChiSquared Test results for a given group with `TestsByGroup`;

9 - (optional) Get confidence intervals for statistics of a given group calculated over bootstrap resamples with `CIsByGroup` and `BenfCIsByGroup`.

#### Resources

***1 - Upload***

To submit directly a CSV file using a POST request. Please use ```accData``` as parameter to submit it.

Endpoint:

    POST /api/acc/Upload

Example Response:

    { 'job': '53fbd735-f787-444c-a701-f65f4887be2e' }

Attributes:

    job string -- The job ID to be used to process the CSV file submitted.

***2 - URL***

To submit a CSV URL using a POST request. Please use ```accURL``` as parameter to submit it.

Endpoint:

    POST /api/acc/URL

Example Response:

    { 'job': '53fbd735-f787-444c-a701-f65f4887be2e' }

Attributes:

    job string -- The job ID to be used to process the CSV file submitted.

***3 - Load***

To actually load the data submitted with ```Direct``` or ```URL``` into Spark for further computations.

This is a ***required*** step in the processing pipeline.

Endpoint:

    GET /api/:job/Load

Example Response:

    { 'job': '53fbd735-f787-444c-a701-f65f4887be2e' }

Attributes:

    job string -- The job ID to be used to process the CSV file submitted.

***4 - Groups***

To get all existing groups in th CSV file, assuming there is a hierarchical structure associated with its values. For further details, please check Benford Analysis for Spark package documentation.

For a plain CSV file with one value per line, it will return only the default group, that is, group 0.

Endpoint:

    GET /api/:job/Groups

Example Response:

```
[ { 'children': [1, 4], 'id': 0, 'level': 0, 'name': 'Benford GmbH' },  ... ]
```

Attributes:

    id integer    -- The generated group ID;
    level integer -- The depth of the group in the existing hierarchical structure (from top = 0 to bottom = N);
    name string   -- The name of the group generated by concatenation of its own name with its parents;
    children      -- The IDs of groups one level below the given group.

***5 - Calc***

To generate bootstrap resamples given a desired number of samples. In bootstrap procedures, it is usual to perform 25,000 resamples. For learning or demonstration purposes, though, 1,000 resamples should suffice.

This is a ***required*** step in the processing pipeline.

Endpoint:

    GET /api/:job/Calc/:samples

Example Response:

    { 'calc':  'ok' }

Attributes:

    calc string -- Returned status of the calculation.

***6 - FreqByGroup***

To get observed frequencies in a given group for both first and second leading digits.

Endpoint:

    GET /api/:job/FreqByGroup/:group

Example Response:

```
[{'count': 1000,
  'd1': [0.296, ... ],
  'd2': [0.114, ... ],
  'd1d2': [0.038, ... ]}]
```

Attributes:

    count integer      -- The number of valid values in the group;
    d1 array[double]   -- Frequencies for the observed leading first digits (1 to 9);
    d2 array[double]   -- Frequencies for the observed leading second digits (0 to 9);
    d1d2 array[double] -- Frequencies for the observed leading first AND second digits (10 to 99).

***7 - TestsByGroup***

To get Z-Test and Chi-Squared Test results for the frequencies in a given group.

Endpoint:

    GET /api/:job/TestsByGroup/:group

Example Response:

```
{'chisquared': [{'count': 1000,
   'testD1': {'elements': [0],
    'pvalues': [0.6724216974515361],
    'rejected': [],
    'stats': [9.177085602172236]},
   'testD1D2': {'elements': [0],
    'pvalues': [0.3744599742230443],
    'rejected': [],
    'stats': [84.15070439060798]},
   'testD2': {'elements': [0],
    'pvalues': [0.29907210527499933],
    'rejected': [],
    'stats': [6.384266949377128]}}],
 'z': [{'count': 1000,
   'testD1': {'elements': [1, 2, 3, 4, 5, 6, 7, 8, 9],
    'pvalues': [0.3774085523788604, ... ],
    'rejected': [],
    'stats': [0.3122939930900707, ... ]},
   'testD1D2': {'elements': [10, ..., 99],
    'pvalues': [0.32303877679980464, ... ],
    'rejected': [],
    'stats': [0.4592181005897983, ... ]},
   'testD2': {'elements': [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
    'pvalues': [0.3069234718369198, ... ],
    'rejected': [],
    'stats': [0.5045898451915904, ...]}}]}
```

Attributes:

    chisquared json         -- Stats and results for the Chi-Squared Test;
    z json                  -- Stats and results for the Z-Test;
    count integer           -- The number of values used in the test;
    testD1 json             -- Stats and results for the first leading digits;
    testD2 json             -- Stats and results for the second leading digits;
    testD1D2 json           -- Stats and results for both first AND second leading digits;
    elements array[integer] -- Elements (first, second or both digits) tested;
    pvalues array[double]   -- p-values for each element tested;
    stats array[double]     -- Value of the corresponding statistic (Z or ChiSquared) for each element tested;
    rejected array[integer] -- Elements which its null hypothesis have been rejected => suspicious.

***8 - CIsByGroup / BenfCIsByGroup***

To get confidence intervals for statistics computed over resamples. The CIs may be computed over resamples drawn from observed frequencies (CIsByGroup) or from an actual Benford distribution (BenfCIsByGroup). The overlapping (or not) of these CIs is used to compute the final results. For further details, please refer to Benford Analysis for Spark package documentation.

Endpoints:

    GET /api/:job/CIsByGroup/:group
    GET /api/:job/BenfCIsByGroup/:group

Example Response:

```
  [{'id': 0,
    'level': 0,
    'CIs': {'d1':
     'n': 1000,
     {'kurtosis': [{'alpha': 0.975,
      'li': 20.6704288774,
      'lower': -0.7184871102,
      't0': -0.4674906641,
      'ui': 994.2121646101,
      'upper': -0.1718710033},
     {'alpha': 0.99,
      'li': 9.7728572862,
      'lower': -0.74298615,
      't0': -0.4674906641,
      'ui': 998.8320954608,
      'upper': -0.1172944659}],
    'mean': [{'alpha': 0.975, ... }],
    'skewness': [{'alpha': 0.975, ... }],
    'variance': [{'alpha': 0.975, ,,, }]},
   'd1d2': { ... },
   'd2': { ... },
   'r': { 'n': 1000,
    'alpha0': [{'alpha': 0.975, ... }],
    'alpha1': [{'alpha': 0.975, ... }],
    'beta0': [{'alpha': 0.975, ... }],
    'beta1': [{'alpha': 0.975, ... }],
    'pearson': [{'alpha': 0.975, ... }]}}}]
```

Attributes:

    id integer    -- The generated group ID;
    level integer -- The depth of the group in the existing hierarchical structure (from top = 0 to bottom = N);
    CIs json      -- Confidence intervals
    d1 json       -- CIs for the first leading digits;
    d2 json       -- CIs for the second leading digits;
    d1d2 json     -- CIs for both first AND second leading digits;
    n integer     -- The number of values used in the calculation;
    mean json     -- CIs for mean
    variance json -- CIs for variance
    skewness json -- CIs for skewness
    kurtosis json -- CIs for kurtosis
    r json        -- CIs for regression coefficients between first and second leading digits;
    alpha0 json   -- CIs for intercept of regression between first and second leading digits;
    alpha1 json   -- CIs for slope of regression between first and second leading digits;
    beta0 json    -- CIs for intercept of regression between second and first leading digits;
    beta1 json    -- CIs for slope of regression between second and first leading digits;
    pearson json  -- CIs for Pearson correlation between first and second leading digits;
    alpha double  -- (1 - significance level / 2);
    li double     -- corresponding element index of lowerbound;
    ui double     -- corresponding element index of upperbound
    lower double  -- CI lowerbound;
    upper double  -- CI upperbound;
    t0 double     -- statistic computed on the original data.

***9 - ResultsByGroup***

To get results for a given group based on the computed confidence intervals.

Endpoint:

    GET /api/:job/ResultsByGroup/:group

Example Response:

```
[{'id': 0,
  'level': 0,
  'n': 1000,
  'results': {'d1': {'kurtosis': {'contains': True, 'overlaps': True},
    'mean': {'contains': True, 'overlaps': True},
    'skewness': {'contains': True, 'overlaps': True},
    'variance': {'contains': True, 'overlaps': True}},
   'd1d2': { ... },
   'd2': { ... },
   'reg': {'alpha0': {'contains': True, 'overlaps': True},
    'alpha1': {'contains': True, 'overlaps': True},
    'beta0': {'contains': True, 'overlaps': True},
    'beta1': {'contains': True, 'overlaps': True},
    'pearson': {'contains': True, 'overlaps': True}},
   'regsDiag': 1,
   'statsDiag': 1}}]
```

Attributes:

    id integer        -- The generated group ID;
    level integer     -- The depth of the group in the existing hierarchical structure (from top = 0 to bottom = N);
    n integer         -- The number of values used in the calculation;
    results json      -- Results
    d1 json           -- Results for the first leading digits;
    d2 json           -- Results for the second leading digits;
    d1d2 json         -- Results for both first AND second leading digits;
    reg json          -- Results for regression between first and second leading digits;
    mean json         -- Results for mean
    variance json     -- Results for variance
    skewness json     -- Results for skewness
    kurtosis json     -- Results for kurtosis
    alpha0 json       -- Results for intercept of regression between first and second leading digits;
    alpha1 json       -- Results for slope of regression between first and second leading digits;
    beta0 json        -- Results for intercept of regression between second and first leading digits;
    beta1 json        -- Results for slope of regression between second and first leading digits;
    pearson json      -- Results for Pearson correlation between first and second leading digits;
    overlaps boolean  -- TRUE if CIs estimated based on your data's and Benford's distributions overlap;
    contains boolean  -- TRUE if CI estimated based on your data's distribution contains actual Benford parameter;
    statsDiag integer -- -1 if you CANNOT infer that your data is a sample drawn from a sample distribution => (possible fraud), 1 otherwise;
    regsDiag integer  -- -1 if you CANNOT infer that your data is a sample drawn from a sample distribution => (possible fraud), 0 if it is undefined and 1 otherwise.
