package org.interledger.ilp.ledger.api.handlers;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
//import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import org.interledger.ilp.common.api.ProtectedResource;
import org.interledger.ilp.common.api.handlers.RestEndpointHandler;
import org.interledger.ilp.common.api.util.VertxUtils;
import org.interledger.ilp.ledger.account.LedgerAccountManager;


/**
 * Accounts handler
 *
 * @author mrmx
 */
public class AccountsHandler extends RestEndpointHandler implements ProtectedResource {
    
    private LedgerAccountManager ledgerAccountManager;    
    
    public AccountsHandler() {
        super("accounts");
    }
    
    public AccountsHandler with(LedgerAccountManager ledgerAccountManager) {
        this.ledgerAccountManager = ledgerAccountManager;
        return this;
    }

    public static AccountsHandler create() {
        return new AccountsHandler();
    }

    @Override
    protected void handleGet(RoutingContext context) {
//        User user = context.user();
        //TODO check admin roles
        JsonObject request = VertxUtils.getBodyAsJson(context);
        int page = request.getInteger("page",1);
        int pageSize = request.getInteger("pageSize",10);
        context.response()
                .putHeader("content-type", "application/json; charset=utf-8") //TODO create decorator
                .end(Json.encode(ledgerAccountManager.getAccounts(page,pageSize)));
    }

}