package org.interledger.ilp.ledger.api.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
//import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.PUT;

import org.interledger.cryptoconditions.Fulfillment;
import org.interledger.cryptoconditions.FulfillmentFactory;
import org.interledger.cryptoconditions.types.MessagePayload;
import org.interledger.ilp.common.api.ProtectedResource;
import org.interledger.ilp.common.api.auth.impl.SimpleAuthProvider;
import org.interledger.ilp.common.api.core.InterledgerException;
import org.interledger.ilp.common.api.handlers.RestEndpointHandler;
import org.interledger.ilp.core.ConditionURI;
import org.interledger.ilp.core.FulfillmentURI;
import org.interledger.ilp.core.LedgerTransfer;
import org.interledger.ilp.core.TransferID;
import org.interledger.ilp.ledger.impl.simple.SimpleLedgerTransfer;
import org.interledger.ilp.ledger.impl.simple.SimpleLedgerTransferManager;
import org.interledger.ilp.ledger.transfer.LedgerTransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fulfillment (and rejection) handler
 *
 * @author earizon
 * 
 * REF: five-bells-ledger/src/controllers/transfers.js
 */
public class FulfillmentHandler extends RestEndpointHandler implements ProtectedResource {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentHandler.class);
    private final static String transferUUID= "transferUUID";

	// GET|PUT /transfers/25644640-d140-450e-b94b-badbe23d3389/fulfillment
	// PUT /transfers/4e36fe38-8171-4aab-b60e-08d4b56fbbf1/rejection

    public FulfillmentHandler() {
       // REF: _makeRouter @ five-bells-ledger/src/lib/app.js
        super("transfer", new String[] 
            {
                "transfers/:" + transferUUID + "/fulfillment",
                "transfers/:" + transferUUID + "/rejection",
            });
        accept(GET, PUT);
    }

    public static FulfillmentHandler create() {
        return new FulfillmentHandler(); // TODO: return singleton?
    }

    @Override
    protected void handlePut(RoutingContext context) {
        // FIXME: If debit's account owner != request credentials throw exception.
        // PUT /transfers/25644640-d140-450e-b94b-badbe23d3389/fulfillment
        // PUT /transfers/4e36fe38-8171-4aab-b60e-08d4b56fbbf1/rejection
        log.info(this.getClass().getName() + "handlePut invoqued ");
        // boolean isFulfillment = false, isRejection   = false;
//        if (context.request().path().endsWith("/fulfillment")){
//            isFulfillment = true;
//        } else if (context.request().path().endsWith("/rejection")){
//            isRejection = true;
//        } else {
//            throw new RuntimeException("path doesn't match /fulfillment | /rejection");
//        }
        /**********************
         * PUT/GET fulfillment (FROM ILP-CONNECTOR)
         *********************
         *
         * PUT /transfers/25644640-d140-450e-b94b-badbe23d3389/fulfillment HTTP/1.1
         *     HTTP 1.1 200 OK
         *     cf:0:ZXhlY3V0ZQ
         * 
         * GET /transfers/25644640-d140-450e-b94b-badbe23d3389/fulfillment HTTP/1.1
         *     HTTP 1.1 200 OK
         *     cf:0:ZXhlY3V0ZQ
         */
        TransferID transferID = new TransferID(context.request().getParam(transferUUID));
        LedgerTransferManager tm = SimpleLedgerTransferManager.getSingleton();
        /*
         * REF: https://gitter.im/interledger/Lobby
         * Enrique Arizon Benito @earizon 17:51 2016-10-17
         *     Hi, I'm trying to figure out how the five-bells-ledger implementation validates fulfillments. 
         *     Following the node.js code I see the next route:
         *     
         *          router.put('/transfers/:id/fulfillment', transfers.putFulfillment)
         *     
         *     I understand the fulfillment is validated at this (PUT) point against the stored condition 
         *     in the existing ":id" transaction.
         *     Following the stack for this request it looks to me that the method
         *     
         *     (/five-bells-condition/index.js)validateFulfillment (serializedFulfillment, serializedCondition, message)
         *     
         *     is always being called with an undefined message and so an empty one is being used.
         *     I'm missing something or is this the expected behaviour?
         * 
         * Stefan Thomas @justmoon 18:00 2016-10-17
         *     @earizon Yes, this is expected. We're using crypto conditions as a trigger, not to verify the 
         *     authenticity of a message!
         *     Note that the actual cryptographic signature might still be against a message - via prefix 
         *     conditions (which append a prefix to this empty message)
         **/
        LedgerTransfer transfer = tm.getTransferById(transferID);
        if (ConditionURI.EMPTY.equals(transfer.getURIExecutionCondition())){
            throw new InterledgerException(
                    InterledgerException.RegisteredException.TransferNotConditionalError,
                    "Transfer is not conditional");
        }
        String   fulfillmentURI = context.getBodyAsString();
        System.out.println("deleteme fulfillmentURI: "+fulfillmentURI);
        Fulfillment          ff = FulfillmentFactory.getFulfillmentFromURI(fulfillmentURI);
        MessagePayload message = new MessagePayload(new byte[]{});
        boolean ffExisted = false;
        System.out.println("deleteme transfer.getURIExecutionCondition().URI:"+transfer.getURIExecutionCondition().URI.toString());
        System.out.println("deleteme transfer.getURICancellationCondition().URI:"+transfer.getURICancellationCondition().URI.toString());
        System.out.println("deleteme request fulfillmentURI:"+fulfillmentURI);
        System.out.println("deleteme request ff.getCondition().toURI():"+ff.getCondition().toURI());

        if (/*isFulfillment && */transfer.getURIExecutionCondition().URI.equals(ff.getCondition().toURI()) ) {
            ffExisted = transfer.getURIExecutionFulfillment().URI.equals(fulfillmentURI);
            if (!ffExisted) {
                if (!ff.validate(message)){
                    throw new RuntimeException("execution fulfillment doesn't validate");
                }
                tm.executeRemoteILPTransfer(transfer, new FulfillmentURI(fulfillmentURI));

            }
        } else if (/*isRejection && */transfer.getURICancellationCondition().URI.equals(ff.getCondition().toURI()) ){
            ffExisted = transfer.getURICancellationFulfillment().URI.equals(fulfillmentURI);
            if (!ffExisted) {
                if (!ff.validate(message)){
                    throw new RuntimeException("cancelation fulfillment doesn't validate");
                }
                tm.abortRemoteILPTransfer(transfer, new FulfillmentURI(fulfillmentURI));
            }
        } else {
            throw new InterledgerException(InterledgerException.RegisteredException.UnmetConditionError, "Fulfillment does not match any condition");
            // throw new InterledgerException(InterledgerException.RegisteredException.InvalidFulfillmentError);
        }
        System.out.println("deleteme ffExisted:"+ffExisted);

        context.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
            .putHeader(HttpHeaders.CONTENT_LENGTH, ""+fulfillmentURI.length())
            .setStatusCode(!ffExisted ? HttpResponseStatus.CREATED.code() : HttpResponseStatus.OK.code())
            .end(fulfillmentURI);
        String notification = ((SimpleLedgerTransfer) transfer).toILPJSONStringifiedFormat();
        TransferWSEventHandler.notifyILPConnector(context, notification);
    }

    @Override
    protected void handleGet(RoutingContext context) {
        // GET /transfers/25644640-d140-450e-b94b-badbe23d3389/fulfillment 
        //                                                    /rejection
        log.info(this.getClass().getName() + " handleGet invoqued ");
        SimpleAuthProvider.SimpleUser user = (SimpleAuthProvider.SimpleUser) context.user();
        boolean isAdmin = user.hasRole("admin");
        boolean transferMatchUser = true; // FIXME: TODO: implement
        if (!isAdmin && !transferMatchUser) {
            throw new InterledgerException(InterledgerException.RegisteredException.ForbiddenError);
        }
        boolean isFulfillment = false; // false => isRejection
        if (context.request().path().endsWith("/fulfillment")){
            isFulfillment = true;
        } else if (context.request().path().endsWith("/rejection")){
            isFulfillment = false;
        } else {
            throw new RuntimeException("path doesn't match /fulfillment | /rejection");
        }
        LedgerTransferManager tm = SimpleLedgerTransferManager.getSingleton();
        TransferID transferID = new TransferID(context.request().getParam(transferUUID));
        LedgerTransfer transfer = tm.getTransferById(transferID);
        if (ConditionURI.EMPTY.equals(transfer.getURIExecutionCondition())){
            throw new InterledgerException(
                    InterledgerException.RegisteredException.TransferNotConditionalError,
                    "Transfer does not have any conditions");
        }
        String fulfillmentURI = (isFulfillment) 
                ? transfer.getURIExecutionFulfillment().URI
                : transfer.getURICancellationFulfillment().URI;
        if ( FulfillmentURI.MISSING.URI.equals(fulfillmentURI)) {
            throw new InterledgerException(
                InterledgerException.RegisteredException.MissingFulfillmentError,
                "This transfer has not yet been fulfilled");
        }
        
        context.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
            .putHeader(HttpHeaders.CONTENT_LENGTH, ""+fulfillmentURI.length())
            .setStatusCode(HttpResponseStatus.OK.code())
            .end(fulfillmentURI);
    }
}

