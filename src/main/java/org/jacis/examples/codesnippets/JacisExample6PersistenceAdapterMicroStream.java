/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.examples.codesnippets.JacisExample1GettingStarted.Account;
import org.jacis.extension.persistence.microstream.MicrostreamPersistenceAdapter;
import org.jacis.extension.persistence.microstream.MicrostreamStorage;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.store.JacisStore;

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * Example 6: MicroStream Persistence Adapter.
 *
 * @author Jan Wiemer
 */
public class JacisExample6PersistenceAdapterMicroStream {

  // Note that we use the same account object introduced for the first example

  public static void main(String[] args) {
    { // first start a container and a store with persistence
      JacisContainer container = new JacisContainer();
      JacisObjectTypeSpec<String, Account, Account> objectTypeSpec //
          = new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
      // start a MicroStream storage manager
      EmbeddedStorageManager storageManager = createMicroStreamStorageManager();
      storageManager.start();
      // create MicroStream storage:
      MicrostreamStorage storage = new MicrostreamStorage(storageManager);
      // create and set the persistence adapter extension
      objectTypeSpec.setPersistenceAdapter(new MicrostreamPersistenceAdapter<>(storage));
      JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();
      // create some objects
      container.withLocalTx(() -> {
        store.update("account1", new Account("account1").deposit(-100));
        store.update("account2", new Account("account2").deposit(10));
        store.update("account3", new Account("account3").deposit(100));
      });
      storageManager.close();
    }
    { // simulate restart and start a new container and a new store
      JacisContainer container = new JacisContainer();
      JacisObjectTypeSpec<String, Account, Account> objectTypeSpec //
          = new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
      // start a MicroStream storage manager
      EmbeddedStorageManager storageManager = createMicroStreamStorageManager();
      storageManager.start();
      // create MicroStream storage:
      MicrostreamStorage storage = new MicrostreamStorage(storageManager);
      // create and set the persistence adapter extension
      objectTypeSpec.setPersistenceAdapter(new MicrostreamPersistenceAdapter<>(storage));
      JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();
      // check the objects are still in the store
      container.withLocalTx(() -> {
        store.stream().forEach(acc -> System.out.println("balance(" + acc.getName() + ")= " + acc.getBalance()));
      });
      storageManager.close();
    }
    System.exit(1);
  }

  protected static EmbeddedStorageManager createMicroStreamStorageManager() {
    EmbeddedStorageManager storageManager = EmbeddedStorageConfiguration.load("microstream.ini") //
        .createEmbeddedStorageFoundation() //
        .createEmbeddedStorageManager();
    return storageManager;
  }

  protected static EmbeddedStorageManager createMicroStreamStorageManagerHardCoded() {
    EmbeddedStorageManager storageManager = EmbeddedStorageConfigurationBuilder.New() //
        .setStorageDirectory("var/data-dir") //
        .setBackupDirectory("var/backup-dir") //
        .createEmbeddedStorageFoundation() //
        .createEmbeddedStorageManager();
    return storageManager;
  }

}