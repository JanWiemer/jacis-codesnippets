/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import java.util.Collection;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.examples.codesnippets.JacisExample1GettingStarted.Account;
import org.jacis.index.JacisNonUniqueIndex;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.store.JacisStore;

/**
 * Example 4: using a tracked view.
 *
 * @author Jan Wiemer
 */
public class JacisExample9Indices {

  // Note that we use the same account object introduced for the first example

  public static void main(String[] args) {
    JacisContainer container = new JacisContainer();
    JacisObjectTypeSpec<String, Account, Account> objectTypeSpec //
        = new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
    JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();

    JacisNonUniqueIndex<Long, String, Account> balanceIdx = store.createNonUniqueIndex("BALANCE-IDX", acc -> acc.getBalance());

    container.withLocalTx(() -> {
      store.update("ACC1", new Account("Acc1").deposit(10l));
      store.update("ACC2", new Account("Acc2").deposit(10l));
      store.update("ACC3", new Account("Acc3").deposit(20l));
    });
    Collection<Account> accounts = balanceIdx.getReadOnly(10l);
    System.out.println("accounts with balance 10 after first TX:");
    accounts.forEach(acc -> System.out.println(" - " + acc.getName()));

    container.withLocalTx(() -> {
      store.update("ACC4", new Account("Acc1").deposit(10l));
      Collection<Account> accounts2 = balanceIdx.getReadOnly(10l);
      System.out.println("accounts with balance 10 in second TX:");
      accounts2.forEach(acc -> System.out.println(" - " + acc.getName()));
    });

  }

}