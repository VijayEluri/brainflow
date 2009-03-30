package brainflow.app.presentation;

import brainflow.app.*;
import brainflow.app.dnd.DnDUtils;
import brainflow.app.toplevel.ImageIOManager;
import brainflow.gui.AbstractPresenter;
import brainflow.gui.FileExplorer;
import brainflow.utils.ResourceLoader;
import brainflow.image.io.IImageDataSource;
import brainflow.image.io.ImageInfo;
import brainflow.image.io.ImageDataSource;
import com.jidesoft.tree.DefaultTreeModelWrapper;
import com.jidesoft.tree.TreeUtils;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.InfiniteProgressPanel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.impl.DefaultFileMonitor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: bradley
 * Date: Nov 19, 2004
 * Time: 11:16:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageFileExplorer extends AbstractPresenter implements TreeSelectionListener, LoadableImageProvider, MouseListener, MouseMotionListener {


    private static final Logger log = Logger.getLogger(ImageFileExplorer.class.getName());

    private FileExplorer explorer;

    private FileSelector selector;

    private ImageIcon folderIcon = new ImageIcon(ResourceLoader.getResource("icons/folder.png"));

    private ImageIcon brickIcon = new ImageIcon(ResourceLoader.getResource("icons/brick.png"));

    private ImageIcon folderOpenIcon = new ImageIcon(ResourceLoader.getResource("icons/folderOpen.png"));

    private ImageIcon imageBucketOpenIcon = new ImageIcon(ResourceLoader.getResource("icons/bin.png"));

    private ImageIcon imageBucketIcon = new ImageIcon(ResourceLoader.getResource("icons/bin_closed.png"));

    private DefaultOverlayable overlayPanel;

    private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel() {
        public Dimension getPreferredSize() {
            return new Dimension(20, 20);
        }
    };

    private JScrollPane scrollPane;


    public ImageFileExplorer(FileObject _rootObject) {


        selector = new CompositeFileSelector(ImageIOManager.getInstance().descriptorArray());


        explorer = new FileExplorer(_rootObject, selector) {
            protected DefaultMutableTreeNode createTreeNode(FileObject fobj) {
                return makeNode(fobj);
            }

        };

        explorer.addTreeSelectionListener(this);

        explorer.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {

                TreePath path = e.getNewLeadSelectionPath();
                if (path != null) {
                    System.out.println("last path component: " + path.getLastPathComponent());
                }
            }
        });

        overlayPanel = new DefaultOverlayable(explorer.getJTree());
        overlayPanel.addOverlayComponent(progressPanel);
        overlayPanel.setOverlayVisible(false);
        progressPanel.stop();

        initTreeExpansionListener();
        initCellRenderer();
        initDnD();

        scrollPane = new JScrollPane(overlayPanel);


    }

    private void initCellRenderer() {
        explorer.getJTree().setCellRenderer(new DefaultTreeCellRenderer() {


            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

                if (value instanceof FolderNode) {
                    if (!expanded) {
                        label.setIcon(folderIcon);
                    } else {
                        label.setIcon(folderOpenIcon);
                    }
                } else if (value instanceof ImageContainerNode) {
                    if (!expanded) {
                        label.setIcon(imageBucketIcon);
                    } else {
                        label.setIcon(imageBucketOpenIcon);
                    }

                } else if (value instanceof ImageLeafNode) {
                    ImageLeafNode node = (ImageLeafNode) value;
                    IImageDataSource limg = node.getUserObject();
                    label.setIcon(brickIcon);

                    if (limg.isLoaded()) {

                    }
                    if (limg.isLoaded() && !selected) {
                        label.setForeground(Color.GREEN.darker().darker());
                    }

                }

                return label;
            }


        });


    }

    private void initTreeExpansionListener() {
        explorer.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeExpanded(TreeExpansionEvent event) {
                LazyNode node = (LazyNode) event.getPath().getLastPathComponent();

                if (!node.areChildrenDefined()) {
                    ImageNodeWorker worker = new ImageNodeWorker(node) {
                        @Override
                        protected void done() {
                            super.done();
                            hideProgressSignal();
                        }
                    };

                    startProgressSignal();

                    try {
                        worker.execute();
                    } catch (Throwable t) {
                        hideProgressSignal();
                        throw new RuntimeException(t);
                    }


                }

            }

            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });

    }

    private void startProgressSignal() {
        progressPanel.start();
        overlayPanel.setOverlayVisible(true);

    }

    private void hideProgressSignal() {
        progressPanel.stop();
        overlayPanel.setOverlayVisible(false);


    }

    private void initDnD() {
        explorer.getJTree().setDragEnabled(true);
        explorer.getJTree().addMouseListener(this);
        explorer.getJTree().addMouseMotionListener(this);

        TransferHandler handler = new TransferHandler() {
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }

            protected Transferable createTransferable(JComponent c) {

                Transferable ret = null;

                if (c instanceof JTree) {
                    JTree tree = (JTree) c;
                    TreePath path = tree.getSelectionPath();
                    Object[] obj = path.getPath();

                    // extracting last object in path ...
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj[obj.length - 1];

                    if (node instanceof DataSourceNode) {
                        DataSourceNode dnode = (DataSourceNode) node;
                        IImageDataSource source = dnode.getUserObject();
                        ret = DnDUtils.createTransferable(source);
                    }


                }

                return ret;
            }

        };

        explorer.getJTree().setTransferHandler(handler);
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
            ImageFileExplorer explorer = new ImageFileExplorer(VFS.getManager().resolveFile("C:/javacode"));


            JFrame frame = new JFrame();
            frame.add(explorer.getComponent(), BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TreeModel getTreeModel() {
        return explorer.getJTree().getModel();

    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public JTree getJTree() {
        return explorer.getJTree();

    }

    public void addFileRoot(FileObject fobj) {
        System.out.println("adding file root " + fobj);
        TreeNode root = (TreeNode) explorer.getJTree().getModel().getRoot();
        Enumeration e = root.children();

        boolean hasnode = false;
        while (e.hasMoreElements()) {
            TreeNode node = (TreeNode) e.nextElement();
            if (node instanceof FolderNode) {
                FolderNode folder = (FolderNode) node;
                if (folder.getFileObject().equals(fobj)) {
                    hasnode = true;
                    //todo this just silently does nothing wheras it might be better to notify user
                    break;
                }
            }
        }

        if (!hasnode)
            explorer.addFileRoot(fobj);
    }

    private void monitorFolder(final FolderNode folder) {
        DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {
            public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {

                folder.resync();
                updateNodeContent(folder);
            }

            public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
                folder.resync();
                updateNodeContent(folder);
            }

            public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {

            }
        });


        fm.addFile(folder.getFileObject());
        fm.start();


    }

    public void valueChanged(TreeSelectionEvent e) {
        requestLoadableImages();
    }


    public FileSelector getSelector() {
        return selector;
    }


    private DefaultTreeModel getDefaultTreeModel() {
        TreeModel model = explorer.getJTree().getModel();
        DefaultTreeModel dtm = null;
        if (model instanceof DefaultTreeModelWrapper) {
            DefaultTreeModelWrapper wrapper = (DefaultTreeModelWrapper) model;

            dtm = (DefaultTreeModel) wrapper.getActualModel();
        } else if (model instanceof DefaultTreeModel) {
            dtm = (DefaultTreeModel) model;
        }

        return dtm;

    }
    public void updateNodeStructure(TreeNode node) {


        DefaultTreeModel dtm = getDefaultTreeModel();

        if (dtm != null) {
            //workaraound for JIDE bug found in forum
            Enumeration<TreePath> state = TreeUtils.saveExpansionStateByTreePath(getJTree());
            dtm.nodeStructureChanged(node);
            TreeUtils.loadExpansionStateByTreePath(getJTree(), state);

        }

    }


    public void updateNodeContent(TreeNode node) {
       DefaultTreeModel dtm = getDefaultTreeModel();

        if (dtm != null) {
            //workaraound for JIDE bug found in forum
            Enumeration<TreePath> state = TreeUtils.saveExpansionStateByTreePath(getJTree());
            dtm.nodeStructureChanged(node);

            //dtm.nodesWereInserted(node);
            TreeUtils.loadExpansionStateByTreePath(getJTree(), state);

        }

    }

    public IImageDataSource[] requestLoadableImages() {
        TreePath[] paths = explorer.getJTree().getSelectionPaths();
        if (paths == null) return new IImageDataSource[0];
        List<IImageDataSource> list = new ArrayList<IImageDataSource>();
        for (int p = 0; p < paths.length; p++) {
            Object[] obj = paths[p].getPath();

            for (int i = 0; i < obj.length; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj[i];
                if (node.isLeaf()) {
                    Object userObject = node.getUserObject();
                    if (userObject instanceof IImageDataSource) {
                        list.add((IImageDataSource) userObject);

                    }
                }
            }
        }

        IImageDataSource[] ret = new IImageDataSource[list.size()];
        list.toArray(ret);
        return ret;
    }

    private MouseEvent firstMouseEvent = null;

    public void mouseClicked(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void mousePressed(MouseEvent e) {
        firstMouseEvent = e;
        e.consume();
    }

    public void mouseDragged(MouseEvent e) {

        if (firstMouseEvent != null) {
            e.consume();
            int dx = Math.abs(e.getX() - firstMouseEvent.getX());
            int dy = Math.abs(e.getY() - firstMouseEvent.getY());
            //Arbitrarily define a 5-pixel shift as the
            //official beginning of a drag.
            if (dx > 5 || dy > 5) {

                //This is a drag, not a click.
                JComponent c = (JComponent) e.getSource();
                //Tell the transfer handler to initiate the drag.
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, firstMouseEvent, -1);
                firstMouseEvent = null;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void mouseReleased(MouseEvent e) {
        firstMouseEvent = null;
    }

    public void mouseEntered(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void mouseExited(MouseEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    class ImageNodeWorker extends SwingWorker<List<LazyNode>, LazyNode> {

        private LazyNode parent;


        public ImageNodeWorker(LazyNode parent) {
            this.parent = parent;
        }

        protected List<LazyNode> doInBackground() throws Exception {
            return parent.fetchChildNodes();


        }

        public void addNode(LazyNode node) {
            publish(node);
        }


        protected void done() {
            try {
                List<LazyNode> result = get();
                for (LazyNode node : result) {
                    parent.add(node);
                }

                updateNodeStructure(parent);

            } catch (InterruptedException e) {
                log.fine("file expansion interrupted");
            } catch (ExecutionException e) {
                log.severe("failed to load all image nodes");
                //throw new RuntimeException(e);
            }


        }

        protected void process(List<LazyNode> chunks) {
            for (LazyNode node : chunks) {
                parent.add(node);
            }



            DefaultTreeModel dtm = getDefaultTreeModel();


            if (dtm != null) {

                dtm.nodeStructureChanged(parent);
            }

        }
    }


    private LazyNode makeNode(FileObject fobj) {

        LazyNode ret = null;

        try {
            if (fobj.getType() == FileType.FOLDER) {
                ret = new FolderNode(fobj);
                monitorFolder((FolderNode)ret);
            } else if (fobj.getType() == FileType.FILE && ImageIOManager.getInstance().isLoadableImage(fobj)) {
                ImageIODescriptor desc = ImageIOManager.getInstance().getDescriptor(fobj);
                IImageDataSource source = desc.createLoadableImage(fobj);
                List<ImageInfo> infoList = source.getImageInfoList();

                assert infoList.size() != 0;

                if (infoList.size() > 1) {
                    ret = new ImageContainerNode(source);
                } else {
                    ret = new ImageLeafNode(source);
                }
            }


        } catch (FileSystemException e) {
            log.severe("failed to load image info for file : " + fobj);
            return null;
            //throw new RuntimeException(e);
        } catch (BrainFlowException e) {
            log.severe("failed to load image info for file : " + fobj);
            //throw new RuntimeException(e);
            return null;
        } catch (RuntimeException e) {
            return null;
        }

        return ret;
    }

    private List<LazyNode> makeNodes(FileObject[] fobj) {
        List<LazyNode> nodeList = new ArrayList<LazyNode>();
        for (FileObject fo : fobj) {
            LazyNode node = makeNode(fo);
            if (node != null) {
                nodeList.add(node);
            }

        }


        return nodeList;

    }

    public abstract class LazyNode extends DefaultMutableTreeNode {

        public abstract List<LazyNode> fetchChildNodes();

        public abstract boolean areChildrenDefined();

        public abstract int priority();
    }

    public abstract class DataSourceNode extends LazyNode {
        public IImageDataSource getUserObject() {
            return (IImageDataSource) super.getUserObject();
        }

    }


    public class ImageContainerNode extends DataSourceNode {

        private List<LazyNode> childNodes = new ArrayList<LazyNode>();

        boolean areChildrenDefined = false;

        public ImageContainerNode(IImageDataSource dataSource) {
            if (dataSource.getImageInfoList().size() < 2) {
                throw new IllegalArgumentException("invalid data source ( < 2 images) for ImageContainerNode");
            }

            setUserObject(dataSource);
        }

        public void setUserObject(Object userObject) {
            if (!(userObject instanceof IImageDataSource)) {
                throw new IllegalArgumentException("user object must be an instance of IImageDataSource");
            }

            super.setUserObject(userObject);
        }

        public boolean areChildrenDefined() {
            return areChildrenDefined;
        }

        public boolean isLeaf() {
            return false;
        }


        public List<LazyNode> fetchChildNodes() {
            if (!areChildrenDefined()) {
                childNodes.clear();
                IImageDataSource parent = getUserObject();
                List<? extends ImageInfo> infoList = parent.getImageInfoList();
                for (ImageInfo info : infoList) {

                    childNodes.add(new ImageLeafNode(new ImageDataSource(parent.getDescriptor(), info)));

                }

                areChildrenDefined = true;
            }

            return childNodes;
        }


        public int priority() {
            return 2;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public class ImageLeafNode extends DataSourceNode {

        private List<LazyNode> childNodes = new ArrayList<LazyNode>();

        boolean areChildrenDefined = false;

        public ImageLeafNode(IImageDataSource dataSource) {
            setUserObject(dataSource);
        }

        public List<LazyNode> fetchChildNodes() {
            return childNodes;
        }

        public void setUserObject(Object userObject) {
            if (!(userObject instanceof IImageDataSource)) {
                throw new IllegalArgumentException("user object must be an instance of IImageDataSource");
            }

            super.setUserObject(userObject);
        }


        public boolean areChildrenDefined() {
            return areChildrenDefined;
        }

        public boolean isLeaf() {
            return true;
        }

        public String toString() {
            return getUserObject().getImageInfo().getImageLabel();
        }

        public int priority() {
            return 3;
        }
    }

    public class FolderNode extends LazyNode {

        private List<LazyNode> childNodes = new ArrayList<LazyNode>();

        boolean areChildrenDefined = false;

        public FolderNode(FileObject folder) {
            try {
                if (folder.getType() != FileType.FOLDER) {
                    throw new IllegalArgumentException("FolderNode only accepts FileObjects that are Folders");
                }

            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
            setUserObject(folder);


        }

        public boolean isLeaf() {
            return false;
        }

        public void resync() {
            if (areChildrenDefined) {
                areChildrenDefined = false;
                childNodes.clear();
                removeAllChildren();
                fetchChildNodes();

                for (int i=0; i<childNodes.size(); i++) {
                    add(childNodes.get(i));
                }
               
            }

        }

        public boolean areChildrenDefined() {
            return areChildrenDefined;
        }

        public FileObject getFileObject() {
            return (FileObject) getUserObject();
        }

        public int priority() {
            return 1;
        }

        private void findChildren() {
            FileObject folder = getFileObject();
            try {
                FileObject[] children = folder.findFiles(getSelector());
                childNodes = makeNodes(children);

            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }

            Collections.sort(childNodes, new Comparator<LazyNode>() {
                public int compare(LazyNode o1, LazyNode o2) {
                    if (o1.priority() > o2.priority()) return 1;
                    if (o2.priority() > o1.priority()) return -1;
                    return 0;

                }
            });

        }

        public List<LazyNode> fetchChildNodes() {
            if (areChildrenDefined) {
                return childNodes;
            }

            findChildren();

            areChildrenDefined = true;
            return childNodes;
        }

        public String toString() {
            if (this == getRoot()) {
                try {
                    URI uri = new URI(getFileObject().getName().getURI());
                    return uri.getHost() + ":" + uri.getPath();
                } catch (URISyntaxException e) {
                    return getFileObject().getName().getURI();
                }
            } else
                return getFileObject().getName().getBaseName();
        }


    }


}
