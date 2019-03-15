import java.security.PublicKey;
import java.util.*;

public class MaxFeeTxHandler {
    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        this.utxoPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        TxHandler handler = new TxHandler(this.utxoPool);
        return handler.isValidTx(tx); // TODO instead of doing this, use inheritance and a base class
    }


    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs, int size) {
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < possibleTxs.length; i++) {
            if(isValidTx(possibleTxs[i])) {
                transactions.add(possibleTxs[i]);
                for(Transaction.Output output : possibleTxs[i].getOutputs()) {
                    utxoPool.addUTXO(new UTXO(possibleTxs[i].getHash(), i), output);
                }
            }
        }

        transactions.sort((o1, o2) -> {
            double inputSum1 = 0;
            double outputSum1 = 0;
            for (Transaction.Input input : o1.getInputs()) {
                Transaction.Output prevOutput = o1.getOutput(input.outputIndex);
                inputSum1 += prevOutput.value;
            }
            for (Transaction.Output output : o1.getOutputs()) {
                outputSum1 += output.value;
            }
            double diff1 = inputSum1 - outputSum1;

            double inputSum2 = 0;
            double outputSum2 = 0;
            for (Transaction.Input input : o2.getInputs()) {
                Transaction.Output prevOutput = o2.getOutput(input.outputIndex);
                inputSum1 += prevOutput.value;
            }
            for (Transaction.Output output : o2.getOutputs()) {
                outputSum2 += output.value;
            }
            double diff2 = inputSum2 - outputSum2;


            return (int)(diff1 - diff2);
        });

        return (Transaction[])transactions.subList(0, size).toArray();
    }
}