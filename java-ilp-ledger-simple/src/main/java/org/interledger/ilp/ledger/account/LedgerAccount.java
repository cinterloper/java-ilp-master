package org.interledger.ilp.ledger.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.money.MonetaryAmount;


/**
 * This interface defines a ledger account.
 *
 * @author mrmx
 */
public interface LedgerAccount {

    String getId();

    String getName();
    
    @JsonIgnore
    String getCurrencyCode();

    LedgerAccount setMinimumAllowedBalance(Number balance);

    LedgerAccount setMinimumAllowedBalance(MonetaryAmount balance);

    MonetaryAmount getMinimumAllowedBalance();

    @JsonProperty("minimum_allowed_balance")
    String getMinimumAllowedBalanceAsString();

    LedgerAccount setBalance(Number balance);

    LedgerAccount setBalance(MonetaryAmount balance);

    MonetaryAmount getBalance();

    @JsonProperty("balance")
    String getBalanceAsString();
    
    @JsonProperty("is_admin")
    Boolean isAdmin();

    @JsonProperty("is_active")
    boolean isActive();
    
    // FIXME: credit & debit not needed must be associated 
    //  to transactions, not Accounts. Accounts must keep only
    // the balance.
    LedgerAccount credit(Number amount);

    LedgerAccount credit(MonetaryAmount amount);

    LedgerAccount debit(Number amount);

    LedgerAccount debit(MonetaryAmount amount);
}
