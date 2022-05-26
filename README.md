##Short introduction

Model-based Testing (MBT) is a popular automated testing approach, used in many industrial settings. 

While MBT is a powerful test-design and test-execution approach, it often suffers from a limitation: when MBT model types are state-charts, when an assertion fails in a given node of a test models, execution of the test model would stop altogether, and test execution would not continue from other parts of the model. This is in contrary to test scripts in JUnit for example, in which if an assertion in a JUnit test method fails, other JUnit test methods will continue their execution. The above issue is also applicable in the [GraphWalker](https://graphwalker.github.io) MBT tool, the focus of the project presented in this repository.

We have come up with two heuristics to tackle the above issue/challenge, as “fault tolerance” features for the MBT tool GraphWalker: (1) when an assertion (developed using Selenium) in a model node fails, go back from the current node to the previous node and execute the failed node / edge again immediately; this is because sometimes fails are undeterministic (doe to the so-called [“flaky” tests](https://www.google.com/search?q=flaky+tests)) and the assertion may pass the second time executing the node; (2) when an assertion in a model node fails, go back from the current node to the previous node and continue the MBT execution from there to other nodes, and making sure to “flag” such failed nodes, and not to visit them again – in a “Black” (no-visit) list.

In this repository, we provide the code and design document for the above two fault-tolerance enhancements.
##Longer introduction and the need for this tool

Model-based Testing (MBT) is a popular automated testing approach, and is used in many industrial settings, e.g., [an experience report from the web applications domain](https://arxiv.org/abs/2104.02152), and [a demo video](https://youtu.be/RizUbMhBTho). There are literally [hundreds of MBT tools available](https://www.google.com/search?q=Model-based+Testing+tool), either as free/open-source or commercial. 

One of the MBT tools, that is used quite widely for testing web and mobile apps, is [GraphWalker](https://graphwalker.github.io). Since GraphWalker is open-source and also given its stability and high-quality, it is one of the best candidate tools for MBT. Like many other MBT tools, the modelling formalism used by GraphWalker is state-charts, i.e., UI flow-diagram of a given web/mobile app. In these test models, nodes are the verifications/assertions to be done in a given web/mobile app page, and edges/transitions are the events which trigger flows between UI screens, e.g., mouse click on a button.

While MBT is a powerful test-design and test-execution approach, it often suffers from a limitation: when an assertion fails in a given node of a test models, execution of the test model would stop altogether, and test execution would not continue from other parts of the model. This is in contrary to test scripts in JUnit for example, in which if an assertion in a JUnit test method fails, other JUnit test methods will continue their execution. 

The above issue is also applicable in the GraphWalker MBT tool. In fact, the development team of GraphWalker has already included a default simplistic fault tolerance mechanism in the GraphWalker code-base, called FailFastStrategy (see below).

<img src="https://github.com/vgarousi/fault-tolerance-for-MBT/blob/0ca3e60dfd6c3152c4552294bab59c8020dd25f8/FailFastStrategy_class.png" />

(The original code listing of the above class can be found in the GraphWalker code-base [via this link]( https://github.com/GraphWalker/graphwalker-project/blob/master/graphwalker-core/src/main/java/org/graphwalker/core/machine/FailFastStrategy.java))

But as we can see, the FailFastStrategy is very simplistic and is not going to be helpful in large MBT projects, since stopping the MBT execution for a single test failure is going to stop the entire MBT suite and stop testing of the other parts of the System Under Test (SUT) using the MBT test suite. Thus, to make MBT more useful and usable for large test automation projects, it is important for test engineers to declare and implement their own fault tolerance mechanisms/ heuristics. That is what we have done in this project. 

We have come up with two heuristics to tackle the above issue/challenge, as “fault tolerance” features for the MBT tool GraphWalker: (1) when an assertion (developed using Selenium) in a model node fails, go back from the current node to the previous node and execute the failed node / edge again immediately; this is because sometimes fails are undeterministic (doe to the so-called [“flaky” tests](https://www.google.com/search?q=flaky+tests)) and the assertion may pass the second time executing the node; (2) when an assertion in a model node fails, go back from the current node to the previous node and continue the MBT execution from there to other nodes, and making sure to “flag” such failed nodes, and not to visit them again – in a “Black” (no-visit) list.
