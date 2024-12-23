# MFDRA-IS: Meta-Features Driven Resource Allocation for Interdependent Systems Modeled by Attack Graphs

# Abstract
Cybersecurity threats are increasingly affecting interdependent systems. This research explores security decision-making in these complex environments and proposes a resource allocation framework managed by a single rational defender to enhance security. We employ attack graphs to model vulnerabilities and present four defense strategies that incorporate network analysis algorithms, including degree centrality, betweenness centrality, harmonic centrality, and TrustRank. The resource allocation methods utilize graph theories such as Adjacent Nodes and Markov Blanket. Each ranking algorithm is paired with these allocation strategies, demonstrating low sensitivity to simultaneous attacks, which aids in developing the performance matrix. We apply random walk node embedding with negative sampling and a gradient descent algorithm for graph-structural feature extraction. Our tabular dataset includes features and ground truth labels based on the optimal resource allocation method for each graph, which we use to train ten classifiers for evaluating unseen data and making predictions. Our framework is validated against two datasets of 100 real-world graphs and 1,000 randomly generated graphs, measuring security improvements against six established baselines, including ARGOSMART and the global best. The results show that our framework performs better compared to these benchmarks, with faster execution than naive methods, and enhances security decision-making across various scenarios, accounting for all attack paths for each entry-asset variation. We have made the full implementation of our resource allocation framework available to the research community for further exploration and development.

# Framework
This framework aims to optimize the security of interdependent systems using genetic algorithm (GA), graph embedding techniques, and classification models. It begins by passing an attack graph with multiple entry nodes and critical assets. GA identifies the most critical attack paths between each entry and asset by using path encoding, crossover, mutation, and a customized fitness function that evaluates asset losses and defense investments. These paths guide resource allocation using methods like TrustRank, Katz centrality, degree centrality, PageRank, and Markov blanket to efficiently allocate defense resources. Also, this framework uses graph embedding to enhance feature learning for resource allocation decisions. Random walks capture structural features of the attack graph, followed by negative sampling and stochastic gradient descent (SGD) to create low-dimensional embeddings for each node. These embeddings, combined with resource allocation methods, form a dataset used to train and evaluate meta-learning classification algorithms (e.g., logistic regression, SVM, and LeNet-5). The goal is to predict the most effective resource allocation strategy efficiently. Performance is evaluated through metrics such as accuracy, hit-at-$k$, and inference time, facilitating optimal security decision-making

![Screenshot (15)](https://github.com/user-attachments/assets/dd3b0d3c-3f75-4b59-966a-43b26dc82d93)

# Customized Fitness Function

$F_2(P) = \max_{P \in P_m} \big(\exp\big(-\sum_{(v_i,v_j)\in P} {x_{i,j}}\big) + Wf\sum_{v_m\in P} L_m\big).$
   
   $P$ is the given attack path.

   $P_m$ is a set of attack paths.

   $v_i,v_j$ are the nodes in $P$.

   $L_m$ is the loss corresponding to node $v_m$

   $Wf$ is the weight factor lies in [0,1]
   
This function accounts for the total asset loss that the system will lose if the attack is occured successfully.

# Our Contribution

- We introduce a meta-learning method for predicting efficient security resource allocation techniques to secure interconnected systems with interdependent assets. Our approach quantifies the improvements of different security resource allocation decisions.

- We implement a node-embedding approach that uses random walks, negative sampling, and gradient descent to capture the attack graph's structural features as meta-features. These meta-features are combined with our performance matrix (representing different allocation methods) to construct a dataset, which is used to train meta-learning classifiers for predicting the most efficient allocation methods.

- We benchmark the performance of seven different graph-theoretic resource allocation methods to counter various attack models aimed at compromising interdependent systems. We also apply six node-ranking algorithms to optimize resource allocation decisions based on the importance of critical assets.

- We assess the effectiveness of our defense strategies across two attack graph testbeds: a real-world testbed with 100 graphs and a synthetic testbed with 1000 graphs. We compare our results against six meta-learning baselines.

- We release our framework's source code to the research community.
