package org.felix.thesis;

public enum TestOutcome {
     firstRequest_tlsAlert_unexpectedMessage,
    secondRequest_tlsAlert_unexpectedMessage,
    secondRequest_noResumption_TLS13,
     firstRequest_tlsAlert_internalError,
    secondRequest_tlsAlert_internalError,
     firstRequest_tlsAlert_unknownCA,
    secondRequest_tlsAlert_unknownCA,
    firstRequest_tlsAlert_unknownName,
    secondRequest_tlsAlert_unknownName,
    firstRequest_tlsAlert_accessDenied,
    secondRequest_tlsAlert_accessDenied,
     firstRequest_tlsAlert_other,
    secondRequest_tlsAlert_other,

     firstRequest_exception_noConnection,
    secondRequest_exception_noConnection,
     firstRequest_exception_other,
    secondRequest_exception_other,

     firstRequest_noApplicationData,
    secondRequest_noApplicationData,

    secondRequest_http200_contentA,
    secondRequest_http200_contentB,
    secondRequest_http200_contentC,
    secondRequest_http200_unknownContent,
     firstRequest_http400_badRequest,
    secondRequest_http400_badRequest,
     firstRequest_http404_notFound,
    secondRequest_http404_notFound,
     firstRequest_http421_misdirectedRequest,
    secondRequest_http421_misdirectedRequest,
     firstRequest_http403_forbidden,
    secondRequest_http403_forbidden,
     firstRequest_httpOther,
    secondRequest_httpOther,

    secondRequest_applicationDataButNoHTTPStatusCode,
     firstRequest_applicationDataButNoHTTPStatusCode,

    debugOutcome_1,
    debugOutcome_2,
    debugOutcome_3,
}