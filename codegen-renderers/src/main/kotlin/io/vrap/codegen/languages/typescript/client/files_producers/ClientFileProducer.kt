package io.vrap.codegen.languages.typescript.client.files_producers

import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.FileProducer

class ClientFileProducer : FileProducer {


    override fun produceFiles(): List<TemplateFile> {
        return listOf(commonTypes(), localCommonTypes(),produceRequestUtils())
    }

    fun commonTypes() = TemplateFile(relativePath = "base/common-types.ts", content = """
        
export type MethodType =
  | "GET"
  | "HEAD"
  | "POST"
  | "PUT"
  | "DELETE"
  | "CONNECT"
  | "OPTIONS"
  | "TRACE";

export type MiddlewareArg = {
  request: ClientRequest;
  response: ClientResponse<any>;
  error: Error;
  next: Middleware;
};

export type ClientRequest = {
  uri: string,
  method: MethodType,
  body?: string | Object,
  headers?: {
    [key: string]: string,
  },
}

export type ClientResponse<T> = {
  body?: T,
  statusCode: number,
  headers?: Object
}

export type Middleware = (arg: MiddlewareArg) => Promise<MiddlewareArg>;

""".trim())
}

fun localCommonTypes() = TemplateFile(relativePath = "base/local-common-types.ts", content = """
import { MethodType } from "./common-types";

export type Variable =  { [key: string]: string | number | boolean }

export interface CommonRequest<T> {
  baseURL: string;
  url?: string,
  headers?: { [key: string]: string };
  method: MethodType;
  uriTemplate: string;
  pathVariables?: Variable;
  queryParams?: Variable;
  body?: T
}
""".trim())

fun produceRequestUtils() = TemplateFile(relativePath = "base/requests-utils.ts", content = """
import { Middleware, MiddlewareArg, ClientResponse } from "./common-types";
import { CommonRequest } from "./local-common-types";

export class ApiRequest<O> {
  private middleware: Middleware;
  constructor(
    private readonly commonRequest: CommonRequest<any>,
    middlewares: Middleware[]
  ) {
    if (!middlewares || middlewares.length == 0) {
      middlewares = [noOpMiddleware];
    }
    this.middleware = middlewares.reduce(reduceMiddleware);
  }

  async execute(): Promise<ClientResponse<O>> {
    const { body, headers, method } = this.commonRequest;
    const req = {
      headers,
      method,
      body,
      uri: getURI(this.commonRequest)
    };

    const res = await this.middleware({
      request: req,
      error: null,
      next: noOpMiddleware,
      response: null
    });

    if (res.error) {
      throw res.error;
    }
    return res.response;
  }
}

function reduceMiddleware(op1: Middleware, op2: Middleware): Middleware {
  return async (arg: MiddlewareArg) => {
    const { next, ...rest } = arg;
    const intermediateOp: Middleware = (tmpArg: MiddlewareArg) => {
      const { next, ...rest } = tmpArg;
      return op2({ ...rest, next: arg.next });
    };

    return op1({
      ...rest,
      next: intermediateOp
    });
  };
}

function getURI(commonRequest: CommonRequest<any>): string {
  const pathMap = commonRequest.pathVariables;
  const queryMap = commonRequest.queryParams;
  var uri: String = commonRequest.uriTemplate;
  var queryParams = [];
  for (const param in pathMap) {
    uri = uri.replace(`{${'$'}{param}}`, `${'$'}{pathMap[param]}`);
  }
  for (const query in queryMap) {
    queryParams = [
      ...queryParams,
      `${'$'}{query}=${'$'}{encodeURIComponent(`${'$'}{queryMap[query]}`)}`
    ];
  }
  const resQuery = queryParams.join("&");
  if (resQuery == "") {
    return `${'$'}{commonRequest.baseURL}${'$'}{uri}`;
  }
  return `${'$'}{commonRequest.baseURL}${'$'}{uri}?${'$'}{resQuery}`;
}

const noOpMiddleware = async (x: MiddlewareArg) => x;
""".trim())