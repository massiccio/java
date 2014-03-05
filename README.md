Java Utils
==========

[![Build Status](https://travis-ci.org/massiccio/java.png?branch=master)](https://travis-ci.org/massiccio/java)

This repository includes some of the code I have written during my academic career (I am slowly adding more content).

The http package includes a NIO-based HTTP load generator.

The math package includes a number of numerical routines, including methods for the Gamma function and the numerical integration, and some optimization algorithms (e.g., binary search).

The queueing package includes exact methods as well as approximations for multi-server queueing models (Erlang-A, Erlang-B, Erlang-C, M/M/n/K, queues with overflow).

The stats package includes some utilities for computing average, variance, etc., as well as some classes for generating numbers according to specific distributions.

The utils package includes a number of utilities:
- I/O utils: methods for finding the tail (last line) and the number of lines (excluding comments) in a text file
- Array list of double (native)
- Create an array of the desired size, initialized at 0
- Code for estimating the probability density function (PDF) as well as the cumulative distribution function (CDF) of a binary file containing double values. This class employs NIO, memory mapped files and byte buffers for high performance. This code was tested with files containing several GB of data.
