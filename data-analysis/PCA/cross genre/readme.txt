Note on description:
monospace terminal part - eigenvalues of covariance matrix

4 x 512 histogram 
125 samples
Means that each track has 125 vectors (each represented as one dot);
 each vector is concatenation of 4 histograms, each histogram having 512 points. I.e. vector length = 2048.

Initial PCM buffer size in this case is 4096;
2048 after reading byte-scattered ints
1024 after averaging over 2 channels;
512 after discarding second symmectrical part of histogram (as DFT is being applied to real values).