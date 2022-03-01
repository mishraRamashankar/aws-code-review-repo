
exports.handler =  function(event, context, callback) {
    var token = event.authorizationToken;
    console.log("User passed token" + token);

    var orderStatusToken = '0af00e4fe2c641bbb9b2c343d7d119d6';  //@TO-DO: need to generate token based on user
    switch (token) {
        case orderStatusToken:
            callback(null, generatePolicy('user', 'Allow', event.methodArn));
            break;
        default:
            callback("Error: Invalid token"); // Return a 4 Invalid token response
    }
};

// Help function to generate an IAM policy
var generatePolicy = function(principalId, effect, resource) {
    console.log("Auth permission: " + effect);
    var authResponse = {};

    authResponse.principalId = principalId;
    if (effect && resource) {
        var policyDocument = {};
        policyDocument.Version = '2012-10-17';
        policyDocument.Statement = [];
        var statementOne = {};
        statementOne.Action = 'execute-api:Invoke';
        statementOne.Effect = effect;
        statementOne.Resource = resource;
        policyDocument.Statement[0] = statementOne;
        authResponse.policyDocument = policyDocument;
    }

    // Optional output with custom properties of the String, Number or Boolean type.
    authResponse.context = {
        "stringKey": "stringval",
        "numberKey": 123,
        "booleanKey": true
    };
    return authResponse;
}