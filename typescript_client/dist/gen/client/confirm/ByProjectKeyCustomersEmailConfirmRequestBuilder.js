"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const requests_utils_1 = require("./../../base/requests-utils");
class ByProjectKeyCustomersEmailConfirmRequestBuilder {
    constructor(args) {
        this.args = args;
    }
    post(methodArgs) {
        return new requests_utils_1.ApiRequest({
            baseURL: 'https://api.sphere.io',
            method: 'POST',
            uriTemplate: '/{projectKey}/customers/email/confirm',
            pathVariables: this.args.pathArgs,
            headers: {
                'Content-Type': 'application/json',
                ...(methodArgs || {}).headers
            },
            body: (methodArgs || {}).body,
        }, this.args.middlewares);
    }
}
exports.ByProjectKeyCustomersEmailConfirmRequestBuilder = ByProjectKeyCustomersEmailConfirmRequestBuilder;
