
# SafetyNet sample

This is a sample for using the SafetyNet API on Android.

First, check the documentation: https://developer.android.com/training/safetynet/attestation

# Sample
If you want to run the sample, just replace the `SAFETY_NET_API_KEY` with your own API key in the `app/build.gradle`

# How to use it in my app
If you want to use the SafetyNet SDK in your app, just add the following dependency in your `app/build.gradle`:
```groovy
implementation "com.google.android.gms:play-services-safetynet:16.0.0"
```
Then, you can take the code from `com.example.safetynet.utils` package and update it as you want.