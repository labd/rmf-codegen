"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const requests_utils_1 = require("./../../base/requests-utils");
class ByProjectKeyMeLoginRequestBuilder {
    constructor(args) {
        this.args = args;
    }
    post(methodArgs) {
        return new requests_utils_1.ApiRequest({
            baseURL: 'https://api.sphere.io',
            method: 'POST',
            uriTemplate: '/{projectKey}/me/login',
            pathVariables: this.args.pathArgs,
            headers: {
                'Content-Type': 'application/json',
                ...(methodArgs || {}).headers
            },
            body: (methodArgs || {}).body,
        }, this.args.middlewares);
    }
}
exports.ByProjectKeyMeLoginRequestBuilder = ByProjectKeyMeLoginRequestBuilder;
