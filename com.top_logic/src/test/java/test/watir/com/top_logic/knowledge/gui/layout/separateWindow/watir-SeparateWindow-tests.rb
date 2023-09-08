#!ruby 
# 
# Watir (http://wtr.rubyforge.org/) tests for the top-logic project. Look in the readme.top-logic.txt in the project 'watir_1_4' for
# documentation.
# 

require 'test/unit'
require 'retry'
require 'watir-tl'
include Watir
include TLStandardTest

# Tests the SeparateWindow-Functionality. Bases heavily on the Top-Logic Layout-Demo from 06.09.2006.
# Tests may fail if configuration in the Top-Logic-Demo changes...
class SeparateWindowTest < TestCase 
  include TLTestSetup
  # needed for TLTestSetup
  def app; 'top-logic' ; end
  
  $SepWinCompBaseURL="http://localhost:8080/top-logic/servlet/LayoutServlet?LAYOUT=masterFrame_3_e_"
  $SepWin1Name="layout.demo.displaySeparateWindow_layoutdemo_separateWindow_sepWin1"
  $SepWin2Name="layout.demo.displaySeparateWindow_layoutdemo_separateWindow_sepWin2"

  def clickSepWinTabber
    # Goto SeparateWindowDemo
    $ie.tabber(:name, "demo.separatewindow.tab",true).click    
  end
  
  
  def testSepWinOpening
    $ie.tabber(:name, "demo.separatewindow.tab",true).click    

    # open first window
    $ie.link(:name, $SepWin1Name,true).click  
    firstWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "0")
    }
    # open second window    
    firstWin.link(:name,$SepWin1Name,true).click
    secondWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "1")
    }
    # open third window    
    secondWin.link(:name,$SepWin1Name,true).click
    thirdWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "2")
    }
    
    # open another window
    $ie.link(:name, $SepWin2Name,true).click  
    otherWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "3")
    }
    
  end
  
  
  def testSepWinClosing
    $ie.tabber(:name, "demo.separatewindow.tab",true).click    
  
    # open first window
    $ie.link(:name, $SepWin1Name,true).click  
    firstWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "0")
    }
    # open second window    
    firstWin.link(:name,$SepWin1Name,true).click
    secondWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "1")
    }
    # open third window    
    secondWin.link(:name,$SepWin1Name,true).click
    thirdWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "2")
    }
    
    # close first window
    firstWin.close()
    
    # look, if the second window is also closed
    begin
      sleep 2 # ie needs some time (actually at least 1 second) to close the window...
      theHopefullyClosedWin = TLIE.attach(:url, $SepWinCompBaseURL + "1")
      raise "Test failed! The second window wasn't closed also it must be closed when closing the first window"     
    rescue NoMatchingWindowFoundException
      # expected  
    end
     # look, if the third window is also closed
    begin
      sleep 2 # ie needs some time (actually at least 1 second) to close the window...
      theHopefullyClosedWin = TLIE.attach(:url, $SepWinCompBaseURL + "2")
      raise "Test failed! The third window wasn't closed also it must be closed when closing the first window"     
    rescue NoMatchingWindowFoundException
      # expected  
    end  
  end
  
  
  
  def testReopeningOfSingleWindow
    $ie.tabber(:name, "demo.separatewindow.tab",true).click    
 
    # open first window
    $ie.link(:name, $SepWin1Name,true).click  
    firstWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "0")
    }
    # open second window    
    firstWin.link(:name,$SepWin1Name,true).click
    secondWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "1")
    }
    
    # reopen first window
    $ie.link(:name, $SepWin1Name,true).click  

    # ensure, that reopening hasn't opened a new window (the first window is configured as "SingleWindow" and must only be focused on reopening...)
    begin
      theHopefullyNotOpenedWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
        TLIE.attach(:url, $SepWinCompBaseURL + "2") # a new window would have been given the index e_2...
      }
      raise "Test failed! The first window was opened more than once. Should not happen with the actual layout-configuration..."     
    rescue RuntimeError
      # expected  
    end
  end
  
  
  def testSepWinClosingOnLogout
    $ie.tabber(:name, "demo.separatewindow.tab",true).click    

    # open a window
    $ie.link(:name, $SepWin1Name,true).click  
    firstWin = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "0")
    }    
    $ie.link(:name, "LogoutLink").click
     # look, if the window is closed
    begin
      sleep 2 # ie needs some time (actually at least 1 second) to close the window...
      theHopefullyClosedWin = TLIE.attach(:url, $SepWinCompBaseURL + "0")
      raise "Test failed! The window wasn't closed also it must be closed when logging out of the application"     
    rescue NoMatchingWindowFoundException
      # expected  
    end
  end
  
  
  def testSepWinClosingOnParentInvisibility
    $ie.tabber(:name, "demo.separatewindow.tab",true).click    
  
    # open window with partnerdemo
    $ie.link(:name, $SepWin2Name,true).click  
    otherWin1 = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "0")
    }
    # open window with partnerdemo
    $ie.link(:name, $SepWin2Name,true).click  
    otherWin2 = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "1")
    }
    # open window with partnerdemo
    $ie.link(:name, $SepWin2Name,true).click  
    otherWin3 = retry_if_exception(NoMatchingWindowFoundException,3,0.5) {
      TLIE.attach(:url, $SepWinCompBaseURL + "2")
    }
    
    $ie.tabber(:name, "demo.layoutRelationManager.tab",true).click    
      
    # look, if the windows are closed
    begin
      sleep 2 # ie needs some time (actually at least 1 second) to close the windows...
      theHopefullyClosedWin = TLIE.attach(:url, $SepWinCompBaseURL + "0")
      theHopefullyClosedWin = TLIE.attach(:url, $SepWinCompBaseURL + "1")
      theHopefullyClosedWin = TLIE.attach(:url, $SepWinCompBaseURL + "2")
      raise "Test failed! The window wasn't closed also it must be closed when logging out of the application"     
    rescue NoMatchingWindowFoundException
      # expected  
    end
  end
end