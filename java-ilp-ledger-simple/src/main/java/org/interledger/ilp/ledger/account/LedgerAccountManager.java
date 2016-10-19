package org.interledger.ilp.ledger.account;

import java.util.Collection;
import org.interledger.ilp.core.AccountUri;

/**
 * Defines account management.
 *
 * @author mrmx
 */
public interface LedgerAccountManager {

    // TODO: recheck whether create is necesary.
    LedgerAccount create(String name);

    int getTotalAccounts();

    void addAccount(LedgerAccount account);

    LedgerAccount getAccountByName(String name) throws AccountNotFoundException;

    Collection<LedgerAccount> getAccounts(int page, int pageSize);
        
    AccountUri getAccountUri(LedgerAccount account);
    
    LedgerAccount getHOLDAccountILP();
}
