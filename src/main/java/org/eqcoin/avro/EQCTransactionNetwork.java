/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.eqcoin.avro;

@org.apache.avro.specific.AvroGenerated
public interface EQCTransactionNetwork {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"EQCTransactionNetwork\",\"namespace\":\"org.eqcoin.avro\",\"types\":[{\"type\":\"record\",\"name\":\"O\",\"fields\":[{\"name\":\"o\",\"type\":\"bytes\"}]}],\"messages\":{\"ping\":{\"request\":[],\"response\":\"O\"},\"registerSP\":{\"request\":[{\"name\":\"S\",\"type\":\"O\"}],\"response\":\"O\"},\"getSPList\":{\"request\":[{\"name\":\"F\",\"type\":\"O\"}],\"response\":\"O\"},\"sendTransaction\":{\"request\":[{\"name\":\"T\",\"type\":\"O\"}],\"response\":\"O\"},\"getPendingTransactionList\":{\"request\":[{\"name\":\"I\",\"type\":\"O\"}],\"response\":\"O\"},\"getTransactionIndexList\":{\"request\":[{\"name\":\"T\",\"type\":\"O\"}],\"response\":\"O\"},\"getTransactionList\":{\"request\":[{\"name\":\"L\",\"type\":\"O\"}],\"response\":\"O\"}}}");
  /**
   */
  org.eqcoin.avro.O ping();
  /**
   */
  org.eqcoin.avro.O registerSP(org.eqcoin.avro.O S);
  /**
   */
  org.eqcoin.avro.O getSPList(org.eqcoin.avro.O F);
  /**
   */
  org.eqcoin.avro.O sendTransaction(org.eqcoin.avro.O T);
  /**
   */
  org.eqcoin.avro.O getPendingTransactionList(org.eqcoin.avro.O I);
  /**
   */
  org.eqcoin.avro.O getTransactionIndexList(org.eqcoin.avro.O T);
  /**
   */
  org.eqcoin.avro.O getTransactionList(org.eqcoin.avro.O L);

  @SuppressWarnings("all")
  public interface Callback extends EQCTransactionNetwork {
    public static final org.apache.avro.Protocol PROTOCOL = org.eqcoin.avro.EQCTransactionNetwork.PROTOCOL;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void ping(org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void registerSP(org.eqcoin.avro.O S, org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getSPList(org.eqcoin.avro.O F, org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void sendTransaction(org.eqcoin.avro.O T, org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getPendingTransactionList(org.eqcoin.avro.O I, org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getTransactionIndexList(org.eqcoin.avro.O T, org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void getTransactionList(org.eqcoin.avro.O L, org.apache.avro.ipc.Callback<org.eqcoin.avro.O> callback) throws java.io.IOException;
  }
}