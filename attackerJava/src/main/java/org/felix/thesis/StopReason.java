package org.felix.thesis;

enum StopReason {
    firstRequest_tlsAlert_unexpectedMessage,
    secondRequest_tlsAlert_unexpectedMessage,
    firstRequest_tlsAlert_internalError,
    secondRequest_tlsAlert_internalError,

    secondRequest_http200_contentA,
    secondRequest_http200_contentB,
    firstRequest_http404_notFound,
    secondRequest_http404_notFound,
    firstRequest_http421_misdirecredRequest,
    secondRequest_http421_misdirecredRequest,
    firstRequest_http403_forbidden,
    secondRequest_http403_forbidden,
    firstRequest_httpOther,
    secondRequest_httpOther,
}