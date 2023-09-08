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
class PopupSelectrTest < TestCase 
  include TLTestSetup
  # needed for TLTestSetup
  def app; 'top-logic' ; end
  
  $SepWinCompBaseURL="http://localhost:8080/top-logic/servlet/LayoutServlet?LAYOUT=masterFrame_3_e_"
  $SepWin1Name="layout.demo.displaySeparateWindow_layoutdemo_separateWindow_sepWin1"
  $SepWin2Name="layout.demo.displaySeparateWindow_layoutdemo_separateWindow_sepWin2"

    def testPopupSelect
        $ie.tabber(:name, "demo.ajaxcomplete.tab",true).click    
        $ie.tabber(:name, "demo.ajax.form.tab",true).click    
        # open window with PopupSelect
        #$ie.image(:name, $SepWin2Name,true).click  
        # TODO twi add name-attributes to the images which open the popupselects to address them here...
    end  
  end
end