package fr.redstonneur1256.bot.util;

public class RadixTree {

    private Node root;

    public RadixTree() {
        root = new Node();
    }

    public void set(byte[] key, int bits) {
        Node node = root;
        int bit = 0;

        while (bit < 8 * key.length && bit < bits) {
            if (node.hasValue) {
                return; // lookup will already stop here, no need to allocate nodes further
            }

            node = (key[bit / 8] & (0x80 >> (bit % 8))) != 0 ? node.left() : node.right();

            bit++;
        }

        node.hasValue = true;

        // we can drop the children to free memory since the lookup will stop at the current node
        node.left = null;
        node.right = null;
    }

    public boolean find(byte[] value) {
        Node node = root;
        int bit = 0;

        while (node != null) {
            if (node.hasValue) {
                return true;
            }
            node = (value[bit / 8] & (0x80 >> (bit % 8))) != 0 ? node.left : node.right;
            bit++;
        }
        return false;
    }

    public long getNodeCount() {
        return root.getNodeCount();
    }

    private static class Node {

        private Node left;
        private Node right;
        private boolean hasValue;

        private Node left() {
            if (left == null) {
                left = new Node();
            }
            return left;
        }

        private Node right() {
            if (right == null) {
                right = new Node();
            }
            return right;
        }

        private long getNodeCount() {
            return 1 + (left == null ? 0 : left.getNodeCount()) + (right == null ? 0 : right.getNodeCount());
        }

    }

}
