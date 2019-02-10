package edu.illinois.cs.cogcomp.classification.MVC;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.LabelKeyValuePair;


/**
 * Shaoshi Ling
 * sling3@illinois.edu
 */

public class View {
	static JFrame window,n,m;
	static JPanel myPanel,panel1,panel2,panel3;
	JTextField word;
	JTextArea text,text1;
    
    private JLabel selectedLabel;
    
    public void cTree(){

	    	JTree tree;
	    	n= new JFrame("Tree");
	    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	    	String file="data/temp.txt";
			String line;
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					String[] parts=line.split(",");
					for(int i=0;i<parts.length;i++){
						root.add(new DefaultMutableTreeNode(parts[i]));
					}
				}	
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


	        tree = new JTree(root);
	 
	       
	        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();       
	        //renderer.setLeafIcon(imageIcon);
	         
	        tree.setCellRenderer(renderer);
	        tree.setShowsRootHandles(true);
	        tree.setRootVisible(false);
	        n.add(new JScrollPane(tree));
	         
	        selectedLabel = new JLabel();
	        n.add(selectedLabel, BorderLayout.SOUTH);
	        n.setTitle("JTree Example");       
	        n.setSize(500, 500);
	        n.setVisible(true);
    }
     
    public void wikiTree()
    {
    	JTree tree;
    	n= new JFrame("Tree");
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    	String file="data/simple_English_WikiCate.txt";
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				root.add(new DefaultMutableTreeNode(line.replaceAll("\\d+.*", "").trim()));
			}	
				
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        tree = new JTree(root);
 
       
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();       
        //renderer.setLeafIcon(imageIcon);
         
        tree.setCellRenderer(renderer);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        n.add(new JScrollPane(tree));
         
        selectedLabel = new JLabel();
        n.add(selectedLabel, BorderLayout.SOUTH);
        /*
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                selectedLabel.setText(selectedNode.getUserObject().toString());
            }
        });
         */
        //n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       n.setTitle("JTree Example");       
        n.setSize(500, 500);
        n.setVisible(true);
    }
    
    public void newsTree()
    {
    	JTree tree;
    	n= new JFrame("20NewsGroup Tree");
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    	DefaultMutableTreeNode po = new DefaultMutableTreeNode("politics");
    	DefaultMutableTreeNode re = new DefaultMutableTreeNode("religion");
    	DefaultMutableTreeNode co = new DefaultMutableTreeNode("computer");
    	DefaultMutableTreeNode sp = new DefaultMutableTreeNode("autos.sports");
    	DefaultMutableTreeNode sa = new DefaultMutableTreeNode("sales");
    	DefaultMutableTreeNode sc = new DefaultMutableTreeNode("science");
    	
    	po.add(new DefaultMutableTreeNode("politics guns"));
    	po.add(new DefaultMutableTreeNode( "politics mideast"));
    	po.add(new DefaultMutableTreeNode("politics"));
    	re.add(new DefaultMutableTreeNode("atheism"));
    	re.add(new DefaultMutableTreeNode("society religion christianity christian"));
    	re.add(new DefaultMutableTreeNode("religion"));
    	co.add(new DefaultMutableTreeNode("computer systems ibm pc hardware"));
    	co.add(new DefaultMutableTreeNode("computer systems mac macintosh apple hardware"));
    	co.add(new DefaultMutableTreeNode("computer graphics"));
    	co.add(new DefaultMutableTreeNode("computer windows x windowsx"));
    	sp.add(new DefaultMutableTreeNode("cars"));
    	sp.add(new DefaultMutableTreeNode("motorcycles"));
    	sp.add(new DefaultMutableTreeNode("baseball"));
    	sp.add(new DefaultMutableTreeNode("hockey"));
    	sa.add(new DefaultMutableTreeNode("for sale discount"));
    	sc.add(new DefaultMutableTreeNode("science electronics"));
    	sc.add(new DefaultMutableTreeNode("science cryptography"));
    	sc.add(new DefaultMutableTreeNode("science medicine"));
    	sc.add(new DefaultMutableTreeNode("science space"));

    	root.add(po);
    	root.add(re);
    	root.add(co);
    	root.add(sp);
    	root.add(sa);
    	root.add(sc);
        tree = new JTree(root);
 
       
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();       
        //renderer.setLeafIcon(imageIcon);
         
        tree.setCellRenderer(renderer);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        n.add(new JScrollPane(tree));
         
        selectedLabel = new JLabel();
        n.add(selectedLabel, BorderLayout.SOUTH);
        /*
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                selectedLabel.setText(selectedNode.getUserObject().toString());
            }
        });
         */
        //n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       n.setTitle("JTree Example");       
        n.setSize(500, 500);
        n.setVisible(true);
    }
    
    
    public void YahooDirTree()
    {
    	JTree tree;
    	n= new JFrame("YahoorDie Tree");
        //create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        //create the child nodes
        DefaultMutableTreeNode ah = new DefaultMutableTreeNode("Arts & Humanities");
        DefaultMutableTreeNode be = new DefaultMutableTreeNode("Business & Economy");
        DefaultMutableTreeNode ci = new DefaultMutableTreeNode("Computer & Internet");
        DefaultMutableTreeNode ed = new DefaultMutableTreeNode("Education");
        DefaultMutableTreeNode en = new DefaultMutableTreeNode("Entertainment");
        DefaultMutableTreeNode go = new DefaultMutableTreeNode("Government");
        DefaultMutableTreeNode he = new DefaultMutableTreeNode("Health");
        DefaultMutableTreeNode nm = new DefaultMutableTreeNode("News & Media");
        DefaultMutableTreeNode rs = new DefaultMutableTreeNode("Recreation & Sports");
        DefaultMutableTreeNode re = new DefaultMutableTreeNode("Reference");
        DefaultMutableTreeNode rg = new DefaultMutableTreeNode("Regional");
        DefaultMutableTreeNode sc = new DefaultMutableTreeNode("Science");
        DefaultMutableTreeNode ss = new DefaultMutableTreeNode("Social Science");
        DefaultMutableTreeNode scu = new DefaultMutableTreeNode("Society & Culture");
        DefaultMutableTreeNode reg = new DefaultMutableTreeNode("Regions");
        DefaultMutableTreeNode sp = new DefaultMutableTreeNode("Sports");
        String file="data/yahooDir.txt";
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if(line.contains("Arts & Humanities")) ah.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Business & Economy")) be.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Computer & Internet")) ci.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Education")) ed.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Entertainment")) en.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Government")) go.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Health")) he.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("News & Media")) nm.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Recreation & Sports")) rs.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Reference")) re.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Regional")) rg.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Science")) sc.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Social Science")) ss.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Society & Culture")) scu.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Regions")) reg.add(new DefaultMutableTreeNode(parts[1]));
				if(line.contains("Sports")) sp.add(new DefaultMutableTreeNode(parts[1]));
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        root.add(ah);
        root.add(be);
        root.add(ci);
        root.add(ed);
        root.add(en);
        root.add(go);
        root.add(he);
        root.add(nm);
        root.add(rs);
        root.add(re);
        root.add(rg);
        root.add(sc);
        root.add(ss);
        root.add(scu);
        root.add(reg);
        root.add(sp);
        //create the tree by passing in the root node
        tree = new JTree(root);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();       
 
        tree.setCellRenderer(renderer);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        n.add(new JScrollPane(tree));
         
        selectedLabel = new JLabel();
        n.add(selectedLabel, BorderLayout.SOUTH);
        /*
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                selectedLabel.setText(selectedNode.getUserObject().toString());
            }
        });
         */
        //n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       n.setTitle("JTree Example");       
        n.setSize(500, 500);
        n.setVisible(true);
    }
    
    
    
    
    
	public View(Controller controller){
        window = new JFrame("DatalessHC");
        window.setSize(800, 600);
        myPanel = new JPanel();
        myPanel.setPreferredSize(new Dimension(400,400));
        myPanel.setLayout(null);
		JButton simple_esa=new JButton("Simple ESA");
		simple_esa.setBounds(30,50,150,30);
		simple_esa.addActionListener(simple_esa(controller));
		myPanel.add(simple_esa);
		JButton classify=new JButton("Text Classification");
		classify.setBounds(30,100,150,30);
		classify.addActionListener(classify(controller));
		myPanel.add(classify);
		word = new JTextField("Enter Single Word");
        word.setSize(200,30);
		word.setLocation(200, 50);
		myPanel.add(word);
        window.setContentPane(myPanel);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	
	public void display_esa(List<ConceptData> cDoc){
		   panel1 = new JPanel();
		   final JTextArea textArea = new JTextArea(5, 30);
		   JScrollPane scrollPane = new JScrollPane(textArea);
		   scrollPane.setPreferredSize(new Dimension(500, 400));
		   textArea.setLineWrap(true);
		   textArea.setWrapStyleWord(true);
		   textArea.setEditable(false);
		   scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		   for (ConceptData key : cDoc) {
				textArea.append(key.concept + ", \t" + String.format("%.4f", key.score)+"\n");
			}
		   textArea.setCaretPosition(textArea.getDocument().getLength());
		   //window.setLayout(new FlowLayout());
		   JButton back=new JButton("Go Back");
		   back.setBounds(30,50,550,430);
		   back.addActionListener(back());
		   panel1.add(back);
		   scrollPane.setLocation(30, 150);
		   panel1.add(scrollPane);
		   window.remove(myPanel);
		   window.setContentPane(panel1);
		   window.validate();
           window.repaint();
	
	}
	
	public void classify_panel(Controller controller){
		panel2 = new JPanel();
		panel2.setLayout(null);
		JButton ctree=new JButton("Customized Tree");
		ctree.setBounds(30,50,150,30);
		ctree.addActionListener(classification("temp",controller));
		panel2.add(ctree);
		JButton ctree1=new JButton("Tree");
		ctree1.setBounds(180,50,60,30);
		ctree1.addActionListener(ctree1(controller));
		panel2.add(ctree1);
		JButton ctree2=new JButton("Build");
		ctree2.setBounds(150,20,80,30);
		ctree2.addActionListener(ctree(controller));
		panel2.add(ctree2);
		
		JButton ylabel=new JButton("YahooDir Tree");
		ylabel.setBounds(30,100,150,30);
		ylabel.addActionListener(classification("YahooDir",controller));
		panel2.add(ylabel);
		JButton ylabel1=new JButton("Tree");
		ylabel1.setBounds(180,100,60,30);
		ylabel1.addActionListener(ytree(controller));
		panel2.add(ylabel1);
		
		
		JButton wlabel=new JButton("WikiCate Tree");
		wlabel.setBounds(30,150,150,30);
		wlabel.addActionListener(classification("WikiCate",controller));
		panel2.add(wlabel);
		JButton wlabel1=new JButton("Tree");
		wlabel1.setBounds(180,150,60,30);
		wlabel1.addActionListener(wtree(controller));
		panel2.add(wlabel1);
		
		JButton nlabel=new JButton("20newsgroup Tree");
		nlabel.setBounds(30,200,150,30);
		nlabel.addActionListener(classification("20newsgroups",controller));
		panel2.add(nlabel);
		JButton nlabel1=new JButton("Tree");
		nlabel1.setBounds(180,200,60,30);
		nlabel1.addActionListener(ntree(controller));
		panel2.add(nlabel1);
		
		
		JButton back=new JButton("Go Back");
		back.setBounds(30,250,150,30);
	    back.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {     
	     		   window.remove(panel2);
	    		   window.setContentPane(myPanel);
	    		   window.validate();
	               window.repaint();
		    }
		});
		panel2.add(back);
		text = new JTextArea("Enter Text");
        text.setSize(400,200);
		text.setLocation(250, 50);
		text.setLineWrap(true);
		text.setMaximumSize( text.getPreferredSize() );
		panel2.add(text);
		window.remove(myPanel);
		window.setContentPane(panel2);
		window.validate();
        window.repaint();
	}
   
	public void result_panel(HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth){
		m= new JFrame("Customized Tree");
		m.setSize(500, 300);
        panel3 = new JPanel();
        JTextArea text3 = new JTextArea();
        text3.setSize(400,200);
		text3.setLocation(30, 30);
		text3.setLineWrap(true);
		text3.setMaximumSize( text3.getPreferredSize() );
		int size=labelResultsInDepth.size()-1;
		for(int j=0;j<labelResultsInDepth.get(size).size();j++){
			text3.append("The label name is "+labelResultsInDepth.get(size).get(j).labelName+"\n");
			
		}
		panel3.add(text3);
        m.setContentPane(panel3);
        m.setVisible(true);
	}
	
	public void cTree_panel(final Controller controller){
		m= new JFrame("Customized Tree");
		m.setSize(700, 400);
        panel3 = new JPanel();
        panel3.setPreferredSize(new Dimension(400,400));
        panel3.setLayout(null);
		JButton dump_tree=new JButton("Dump Tree");
		dump_tree.setBounds(30,50,150,30);
		dump_tree.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {     
		    		String text = text1.getText();
		    		controller.costumized_tree(text);
		    		m.dispose();
			    }
			});
		panel3.add(dump_tree);
		text1 = new JTextArea("Enter Text");
        text1.setSize(400,200);
		text1.setLocation(200, 50);
		text1.setLineWrap(true);
		text1.setMaximumSize( text1.getPreferredSize() );
		panel3.add(text1);
        m.setContentPane(panel3);
        m.setVisible(true);
	}
	
	private ActionListener back() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
     		   window.remove(panel1);
    		   window.setContentPane(myPanel);
    		   window.validate();
               window.repaint();
            	
   	
            }
        };
    }
	
    private ActionListener classify(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            	classify_panel(controller);
            }
        };
    }
    
    
    private ActionListener classification(final String s,final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            	try {
            		HashMap<Integer, List<LabelKeyValuePair>> labelResultsInDepth=controller.classify(s,text.getText());
            		result_panel(labelResultsInDepth);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        };
    }
    
    private ActionListener simple_esa(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            
            try {
				controller.simple_esa(word.getText());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
            }
        };
    }


    private ActionListener wtree(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            
            	wikiTree();
   	
            }
        };
    }
    
    private ActionListener ctree(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            
            	cTree_panel(controller);
   	
            }
        };
    }
    
    private ActionListener ntree(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            
            	newsTree();
   	
            }
        };
    }
    
    private ActionListener ytree(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            
            	YahooDirTree();
   	
            }
        };
    }
    
    private ActionListener ctree1(final Controller controller) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            
            	cTree();
   	
            }
        };
    }
}
