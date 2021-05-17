package com.example.sensortrial;

public class FFT {

    // compute the FFT of x[], assuming its length n is a power of 2
    public static ComplexNumber[] fft(ComplexNumber[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new ComplexNumber[]{x[0]};

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // compute FFT of even terms
        ComplexNumber[] even = new ComplexNumber[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        ComplexNumber[] evenFFT = fft(even);

        // compute FFT of odd terms
        ComplexNumber[] odd = even;  // reuse the array (to avoid n log n space)
        for (int k = 0; k < n / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        ComplexNumber[] oddFFT = fft(odd);

        // combine
        ComplexNumber[] y = new ComplexNumber[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            ComplexNumber wk = new ComplexNumber(Math.cos(kth), Math.sin(kth));
            y[k] = ComplexNumber.add(evenFFT[k],ComplexNumber.multiply(oddFFT[k],wk));
            y[k + n / 2] = ComplexNumber.subtract(evenFFT[k],ComplexNumber.multiply(oddFFT[k],wk));
        }
        return y;
    }
}