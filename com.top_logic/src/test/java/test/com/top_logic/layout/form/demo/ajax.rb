#!ruby

require 'watir'
include Watir

def getSubFrames(frame)
  
  frames = Array.new
  i = 0
  begin
    while true
      frames[i] = frame.frame(:index,i+1)
      i = i + 1
    end
  rescue UnknownFrameException => ex
    return frames    
  end
end

def findTextFieldByName(frame,name)
  iteratorFactory = proc {|frame|frame.text_fields}
  filter = proc {|field|field.name == name}
  return findObject(frame,iteratorFactory,filter)
end


def findLinkbyHREF(frame,href)
  iteratorFactory = proc {|frame|frame.links}
  filter = proc {|link|link.href == href}
  return findObject(frame,iteratorFactory,filter)
end

def findImage(frame,filter)
  iteratorFactory = proc {|frame|frame.images}
  return findObject(frame,iteratorFactory,filter)
end

def findSpan(frame,filter)
  iteratorFactory = proc {|frame|frame.spans}
  return findObject(frame,iteratorFactory,filter)
end

def findObject(frame,iteratorFactory,filter)
  
  for obj in iteratorFactory.call(frame)
    return obj if (filter.call(obj))
  end
  
  for subFrame in getSubFrames(frame)
    obj = findObject(subFrame,iteratorFactory,filter) 
    return obj if obj != nil
  end
  
  return nil
  
end

def hasError(ie,text_field)
  
  findOnError = proc {
    |span|
    if (span.class_name =~ /is-onerror/) # identify a tag that displays the error
      
      # check if they are connected through java-script objects
      master = span.getOLEObject.invoke('getAttribute','controller')["master"]["element"]
      if (text_field.getOLEObject().invoke("parentElement")["id"] == master["id"]) 
        # now we know that the error tag and the input tag are connected.
        
        # every connected span wraps another span... we need to examine this further
        # as this span tells us if the connected field has an error. It has an 
        # error if the class of the inner span equals 'is-error'.
        
        # ok lets get the child
        child = span.getOLEObject().invoke('firstChild')
        
        if (child.invoke('className') == 'is-error')
          # jippie now we know that the given text field has an error
          return true
        end        
      end
    end
    
    return false 
  }
  return findSpan(ie,findOnError) != nil
  
end


def login()
  ie = IE.new
  ie.goto('http://localhost:8080/top-logic')
  
  ie.text_field(:index,1).set('root')
  ie.text_field(:index,2).set('123')
  ie.button(:index,1).click()
  return ie
end

def clickTabberAjax(ie)
  link = findLinkbyHREF(ie,'http://localhost:8080/top-logic/servlet/LayoutServlet?LAYOUT=masterFrame_0_t_0_b&tab=14')
  link.click()
end

def action()
  ie = login()
  clickTabberAjax(ie)
  
  
  
  nameTextField = findTextFieldByName(ie,'test.order.purchaser.givenName')
  
  # the error tag should say 'all fine'
  if (not hasError(ie,nameTextField))
    puts 'ok(0)'
  end
  
  # the field only accepts the input if it starts with an uppercase
  nameTextField.set('bernhard') 
  surNameTextField = findTextFieldByName(ie,'test.order.purchaser.surname')
  surNameTextField.focus() # activates the field checking
  
  # now the connected error tag should indicate an error
  if (hasError(ie,nameTextField))
    puts 'ok(1)'
    nameTextField.flash
  end
  
  return ie,nil,nameTextField
end

#action

#ie.close()
