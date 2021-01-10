/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.container.JacisTransactionHandle;
import org.jacis.examples.codesnippets.JacisExample1GettingStarted.Account;
import org.jacis.plugin.JacisModificationListener;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.store.JacisStore;

/**
 * Example 8: Modification Listener.
 *
 * @author Jan Wiemer
 */
public class JacisExample8ModificationListener {

  static class ExampleJacisModificationListener implements JacisModificationListener<String, Account> {

    @Override
    public void onModification(String key, Account oldValue, Account newValue, JacisTransactionHandle tx) {
      System.out.println("modified " + key + ": " + newValue + "(by " + tx + ")");
    }

  }

  public static void main(String[] args) {

    JacisContainer container = new JacisContainer();
    JacisObjectTypeSpec<String, Account, Account> objectTypeSpec = //
        new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
    JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();

    // register example modification listener at the store:
    JacisModificationListener<String, Account> listener = new ExampleJacisModificationListener();
    store.registerModificationListener(listener);

    container.withLocalTx(() -> {
      store.update("account1", new Account("account1").withdraw(100));
    });
    container.withLocalTx(() -> {
      store.update("account1", store.get("account1").deposit(1000));
    });
  }
}
