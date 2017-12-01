- Personal Data extracted is not correct:
    - Watson Knowledge Studio requires rigorous training
  with as much variety of data as possible. Please train the WKS model with a large
  set of data so it can learn and give right results
    - Regular Expressions: Regular expressions can be configured and used for personal
      data that cannot be extracted using NLU with custom WKS model. Configure appropriate
      regular expressions in User Defined Variables of the application
- The application does not show any data
    - This could be possible due to incorrect configuration. Ensure that the User defined
      variable keys follow the same pattern as mentioned provided in the sample
    - Check application log for any errors on the server side
