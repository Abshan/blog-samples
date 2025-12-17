package org.wso2.sample.synapse.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryUtil;

public class CustomSynapseHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(CustomSynapseHandler.class);

    // Give a handler specific SPAN_NAME to identify the handler specific latency
    private static final String SPAN_NAME = "Synapse_Handler:CustomSynapseHandler";

    public boolean handleRequestInFlow(MessageContext synCtx) {
        log.info("Request In Flow");

        // Start handler span
        startHandlerSpan(synCtx);
        try {

            /**
             * Synapse handler processing logic.
             */

            return true;

        } finally {
            // Finish handler span
            finishHandlerSpan(synCtx);
        }
    }

    public boolean handleRequestOutFlow(MessageContext synCtx) {
        log.info("Request Out Flow");
        return true;
    }

    public boolean handleResponseInFlow(MessageContext synCtx) {
        log.info("Response In Flow");
        return true;
    }

    public boolean handleResponseOutFlow(MessageContext synCtx) {
        log.info("Response Out Flow");
        return true;
    }

    /**
     * Starts a new span for this handler and attaches it to the message context.
     */
    private void startHandlerSpan(MessageContext synCtx) {
        if (TelemetryUtil.telemetryEnabled()) {
            TelemetryTracer telemetryTracer = GatewayUtils.getTelemetryTracer();
            TelemetrySpan responseLatencySpan =
                    (TelemetrySpan) synCtx.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);

            TelemetrySpan handlerSpan = TelemetryUtil.startSpan(SPAN_NAME, responseLatencySpan, telemetryTracer);
            TelemetryUtil.setTag(handlerSpan, APIMgtGatewayConstants.SPAN_KIND, APIMgtGatewayConstants.SERVER);

            synCtx.setProperty(SPAN_NAME, handlerSpan);

        } else if (Util.tracingEnabled()) {
            TracingTracer tracer = GatewayUtils.getTracingTracer();
            TracingSpan responseLatencySpan =
                    (TracingSpan) synCtx.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);

            TracingSpan handlerSpan = Util.startSpan(SPAN_NAME, responseLatencySpan, tracer);
            Util.setTag(handlerSpan, APIMgtGatewayConstants.SPAN_KIND, APIMgtGatewayConstants.SERVER);

            synCtx.setProperty(SPAN_NAME, handlerSpan);
        }
    }

    /**
     * Finishes the handler span if present in the message context.
     */
    private void finishHandlerSpan(MessageContext synCtx) {
        if (TelemetryUtil.telemetryEnabled()) {
            TelemetrySpan span = (TelemetrySpan) synCtx.getProperty(SPAN_NAME);
            if (span != null) {
                TelemetryUtil.finishSpan(span);
            }
        } else if (Util.tracingEnabled()) {
            TracingSpan span = (TracingSpan) synCtx.getProperty(SPAN_NAME);
            if (span != null) {
                Util.finishSpan(span);
            }
        }
    }
}
