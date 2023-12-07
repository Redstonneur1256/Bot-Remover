package fr.redstonneur1256.bot.util;

public class AddressTree {

    private RadixTreeNode root;

    public AddressTree() {
        this.root = new RadixTreeNode();
    }

    public void set(byte[] key, byte[] mask) {
        int bit = 0;

        RadixTreeNode node = root;

        while (bit < 8 * key.length && (mask[bit / 8] & (0x80 >> (bit % 8))) != 0) {
            node = (key[bit / 8] & (0x80 >> (bit % 8))) != 0 ? node.left() : node.right();

            bit++;
        }

        node.hasValue = true;

        // we can drop the children to free memory since the lookup will stop at the current node
        node.left = null;
        node.right = null;
    }

    public boolean find(byte[] value) {
        RadixTreeNode node = root;

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

    private static class RadixTreeNode {
        private RadixTreeNode left, right;
        private boolean hasValue;

        private RadixTreeNode left() {
            if (left == null) {
                left = new RadixTreeNode();
            }
            return left;
        }

        private RadixTreeNode right() {
            if (right == null) {
                right = new RadixTreeNode();
            }
            return right;
        }

        private long getNodeCount() {
            return 1 + (left == null ? 0 : left.getNodeCount()) + (right == null ? 0 : right.getNodeCount());
        }

    }
}
