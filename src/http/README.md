Package containing an HTTP load generator.

The LoadGenerator class should be invoked as follows:

java -cp . http.LoadGenerator 10 http/high2.load http://wikipedia.org 2.3

where 10 is the average arrival rate (requests/second), the second value
is the path to a file containing the relative paths, the third value
is the domain, and the fourh one is the squared coefficient of variation
of interarrival intervals (if missing, exponentially distributed random
intervals will be generated).


The Clarknet class adds the options of having a file containing the arrival rates.
This enables one to create time dependent load:

java -cp . http.Clarknet http/load_month.txt http/high2.load http://en.wikipedia.org/

The other arguments are the same as before.


The programs generate reports in the form of log files which become 
visible once the programs end.