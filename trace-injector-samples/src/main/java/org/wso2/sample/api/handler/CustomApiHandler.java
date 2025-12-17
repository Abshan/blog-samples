package org.wso2.sample.api.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryUtil;

import java.util.Map;

public class CustomApiHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(CustomApiHandler.class);
    private static final String SPAN_NAME = "API_Handler:CustomApiHandler";

    public boolean handleRequest(MessageContext msgCtx) {

        log.info("Request Flow");

        // Start handler span
        startHandlerSpan(msgCtx);
        try {

            /**
             * API handler processing logic.
             */

            return true;

        } finally {
            // Finish handler span
            finishHandlerSpan(msgCtx);
        }
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public boolean authenticate(MessageContext synCtx) throws APISecurityException {
        Map headers = getTransportHeaders(synCtx);
        String authHeader = getAuthorizationHeader(headers);
        if (authHeader.startsWith("userName")) {
            return true;
        }
        return false;
    }

    private String getAuthorizationHeader(Map headers) {
        return (String) headers.get("Authorization");
    }

    private Map getTransportHeaders(MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Starts a new span for this handler and attaches it to the message context.
     */
    private void startHandlerSpan(MessageContext msgCtx) {
        if (TelemetryUtil.telemetryEnabled()) {
            TelemetryTracer telemetryTracer = GatewayUtils.getTelemetryTracer();
            TelemetrySpan responseLatencySpan =
                    (TelemetrySpan) msgCtx.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);

            TelemetrySpan handlerSpan = TelemetryUtil.startSpan(SPAN_NAME, responseLatencySpan, telemetryTracer);
            TelemetryUtil.setTag(handlerSpan, APIMgtGatewayConstants.SPAN_KIND, APIMgtGatewayConstants.SERVER);

            msgCtx.setProperty(SPAN_NAME, handlerSpan);

        } else if (Util.tracingEnabled()) {
            TracingTracer tracer = GatewayUtils.getTracingTracer();
            TracingSpan responseLatencySpan =
                    (TracingSpan) msgCtx.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);

            TracingSpan handlerSpan = Util.startSpan(SPAN_NAME, responseLatencySpan, tracer);
            Util.setTag(handlerSpan, APIMgtGatewayConstants.SPAN_KIND, APIMgtGatewayConstants.SERVER);

            msgCtx.setProperty(SPAN_NAME, handlerSpan);
        }
    }

    /**
     * Finishes the handler span if present in the message context.
     */
    private void finishHandlerSpan(MessageContext msgCtx) {
        if (TelemetryUtil.telemetryEnabled()) {
            TelemetrySpan span = (TelemetrySpan) msgCtx.getProperty(SPAN_NAME);
            if (span != null) {
                TelemetryUtil.finishSpan(span);
            }
        } else if (Util.tracingEnabled()) {
            TracingSpan span = (TracingSpan) msgCtx.getProperty(SPAN_NAME);
            if (span != null) {
                Util.finishSpan(span);
            }
        }
    }
}
