/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.visitors;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.nodes.AllNode;
import com.top_logic.base.search.nodes.BinaryFilter;
import com.top_logic.base.search.nodes.BinaryOper;
import com.top_logic.base.search.nodes.ExampleNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryOper;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.filt.DOAttributeComparator;
import com.top_logic.dob.filt.DOAttributeFilter;
import com.top_logic.dob.filt.REFilter;
import com.top_logic.knowledge.service.KBUtils;

/**
 * The Resolver can be used as superclass for domain-specific Searcheables.
 * <p>
 *  This Resolver does not care about Relevance (as stated in the documentation)
 *  since this would result in a slower and more complex implementation.
 *  By default ift will only throw a QueryException on any unsupported node.
 *  It implents Code for some Nodes in a generic Way. You should in any case
 *  think about optimizing them. 
 * </p>
 *   For a most basic Resolver you mus override {@link #getAllElements()}. 
 *   Top optimize you resolver you should use filters on your realm to 
 *   filter the original obejcts instead of the resultiung dataobjects.
 *   This Resolver allows you to put DataObjectFilters on the stack as
 *   a placeholder for the actual Set of Objects.
 * <p>
 *  For the sake of completeness it must be stated that this is a Postfix
 *  vistor using a Stack for its intermediate results.
 * </p>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class Resolver extends AbstractVisitor implements Comparator {
    
    /**  Map of an evtually previous search, may be null. */
    protected   Set         previous;
    
    /** Comparator to be used for internal sorting. */
    protected   Comparator  orderBy;

    /** Root node of the Query to be resolved */
    protected   QueryNode   rootNode;

    /** Results will be accumulated in this Stack of (Sorted)Sets*/
    protected   Stack       results;
    
    /* A Subclass may wish to add the follwing variables;
    
    // Stack of filters agains the Searcheable domain matching the QueryNodes 
    protected Stack filters;
    
    // Some Object representing the searcheable realm (e.g. a KnowledgeBase)
    protected ????  RootObject
    */
    
   /** 
    * This class does not care (here) for the ORDER BY part of the query.
    */
   @Override
protected void visitOrder(String anOrderBy[]) {
       // not done here
   }

    /** Override to catch any QueryNode */
    @Override
	protected void visitQueryNode(QueryNode aNode) 
        throws QueryException
    {
        throw new QueryException("Node type " + aNode + " not supported");
    }
        
    /** create a Resolver for a previous Match for a given Query.
     *
     * @param somePrevious  is the Map of an evtually previous search, may be null.
     * @param aQuery    The Query to be resolved
     *
     */
    public Resolver(Set somePrevious, Query aQuery) {
       
        String []   order    = aQuery.getOrderBy();
        rootNode = aQuery.getRoot();
        orderBy      = null;
        if (order != null) {
            orderBy = new DOAttributeComparator(order);
        }

        this.previous   = somePrevious;
        results = new Stack();
    }
    
    // Minimal Method needed for a Resolver
    
    /**
	 * Get All Elements of the search Realm.
	 * <p>
	 * <b>Warning:</b> This function should only be used for a plain "ALL"-Query (and even than you
	 * should return some cached values). Take care to optimize your Resolver to use some sort of
	 * Filters That effectivly reduce the size of the searched realm !
	 * </p>
	 */
    protected abstract Set getAllElements() throws QueryException;

    
    /** Create an (ordered) Set dependening on the Setup of the Resolver.
     *
     * @param size the Excpected Size of the Set,
     */
    protected Set<DataObject> createSet(int size) {
        if (orderBy == null) {
            return new HashSet<>(size);
        }
        else
            return new TreeSet<DataObject>(this);    
    }
    
    /** Create an (ordered) Set by copying the given set.
     *
     * @param copyFrom The result is copied from this Set.
     */
    protected Set createSet(Set copyFrom) {
        if (copyFrom instanceof SortedSet) {
            return new TreeSet((SortedSet) copyFrom);    
        }
        else    
            return new HashSet(copyFrom);
    }

    // Actual Visitor / Resolver Methods
    
    /** Generic method to resolve an AllNode (which may be a NONE-Node, too) */
    @Override
	public void visitNode(AllNode all)  throws QueryException {
        Set result;
        if (all == AllNode.ALL) {
            if (previous != null) {
                result = previous;
            }
            else 
                result = getAllElements();
        }
        else // must be NONE-Node 
            result = createSet(0);
        results.push(result);
    }

    /** Generic method to visit an ExampleNode.
     *<p>
     *  Extract the Example from the method and put it eith TRUE 
     *  relevance into the resulting map.
     *  I can think of now reason why you should override this method ...
     *</p>
     */
    @Override
	public void visitNode(ExampleNode example) throws QueryException {

        Set result = createSet(1);
        
        result.add(example.getExample());
        
        results.push(result);
    }
   
    /** Generic method to visit an UnaryOperator (!, NOT).
     *<p>
     *  You propably wish to optimize this since it may fall back to
     *  the getAllElements() method.
     *</p>
     */
    @Override
	public void visitNode(UnaryOper anOper) throws QueryException {
    
        anOper.getSubNode().visitNode(this);
        
        if (anOper.getKind() == QueryNode.NOT)  {
            Object  oper = results.pop();
            if (oper instanceof Filter)  {
                results.push(FilterFactory.not((Filter) oper));
            } else {
                Set  operand = (Set) oper;

                Set  all     = previous != null ? previous : getAllElements();
                // Return all Elements not contained in operand 
                // might be optimized some way ...
                Set result = createSet(all);
                result.removeAll(operand);
                results.push(result);
            }
        }
        else {
            super.visitNode(anOper);
        }
    }
    
    //  There is no Generic method to visit an UnaryFilter ("$", "@", "#")
    //  since these are implemenatation specific 
    

    /** Compute the AND operator from the operands found on the Stack. 
     */
    protected void computeAnd() throws QueryException {
    
        Object  oper2 = results.pop();
        Object  oper1 = results.pop();
        
        Set set1, set2;
        if (oper1 instanceof Filter) {
            if (oper2 instanceof Filter) {
                
                // fine, just combine the Filters ....
				results.push(FilterFactory.and((Filter) oper1, (Filter) oper2));
                return; 
            }
            // Resolve set1 ...
            set1 = doFilter((Filter) oper1);
        }
        else {
            set1 = (Set) oper1;
            // In case you fail here you probably need to override computeAnd
            // using your own, special filters ...
        }
        
        if (oper2 instanceof Filter) {
            // Resolve set2
            set2 = doFilter((Filter) oper2);
        }
        else {
            set2 = (Set) oper2;
        }
        
        Set small, large;
        
        // Optimize to use smaller set for iterating
        if (set1.size() < set2.size())  {
            small = set1;
            large = set2;
        }   
        else {
            large = set1;
            small = set2;
        }

        Set result = createSet(small.size());
        Iterator iter = small.iterator();
        while (iter.hasNext()) {
            Object member = iter.next();
            if (large.contains(member)) {
               result.add(member);                               
            }
        }
        results.push(result);
    }
    
    
    /** Compute the OR operator from the operands found on the Stack. 
     */
    protected void computeOr() throws QueryException  {
    
        Object  oper2 = results.pop();
        Object  oper1 = results.pop();
    
        Set set1, set2;
        if (oper1 instanceof Filter)  {
            if (oper2 instanceof Filter) {
            
                // fine, just combine the Filters ....
				results.push(FilterFactory.or((Filter) oper1, (Filter) oper2));
                return; 
            }
            // Resolve set1 ...
            set1 = doFilter((Filter) oper1);
        }
        else
            set1 = (Set) oper1;
            // In case you fail here you probably need to override computeAnd
            // using your own, special filters ...

        if (oper2 instanceof Filter) {
            set2 = doFilter((Filter) oper2);
        }
        else
            set2 = (Set) oper2;
        
        Set small, large;
        
        // Optimize to use smaller set for adding
        if (set1.size() < set2.size())  {
            small = set1;
            large = set2;
        }   
        else {
            large = set1;
            small = set2;
        }

        Set result = createSet(large);
        result.addAll(small);
        
        results.push(result);
    }

    /** return true when the rigt DataObject Matches the Left DataObject.
     *
     * @param attrNames (cached) result from left.getAttributeNames();
     */
    protected boolean matches(DataObject left, String attrNames[], DataObject right) {
        
        int len = attrNames.length;
        for (int i=0; i < len; i++) {
            String attrName = attrNames[i];
            try {
                Object rValue = right.getAttributeValue(attrName);
                Object lValue = left .getAttributeValue(attrName);
                if (!lValue.equals(rValue)) {
                    return false;
                }
            }
            catch (NoSuchAttributeException nsae) {
                return false;   // No Attribute, no match ...
            }
        }
        return true;  // Everything (or an empty Example) matched.
        
    }
    
    /** Compute the MATCHES operator from the operands found on the Stack. 
     */
    protected void computeMatches() throws QueryException  {

        Object  oper2 = results.pop();
        Object  oper1 = results.pop();

        Set set1, set2;
        
        // There is no Matches Filter by now ....
        if (oper1 instanceof Filter) {
            set1 = doFilter((Filter) oper1);
        }
        else
            set1 = (Set) oper1;
        
        if (oper2 instanceof Filter) {
            set2 = doFilter((Filter) oper2);
        }
        else
            set2 = (Set) oper2;
        
        Set small, large;
        
        // Optimize to use smaller set for matching
        if (set1.size() < set2.size())  {
            small = set1;
            large = set2;
        }   
        else {
            large = set1;
            small = set2;
        }
                    // assume a match Rate of 25 % ...
        Set result = createSet(large.size() >> 2);
        Iterator outer = small.iterator();
        while (outer.hasNext()) {
            DataObject left = (DataObject) outer.next();
            String [] attrNames = left.getAttributeNames ();
            Iterator  inner = large.iterator();
            while (inner.hasNext()) {
                DataObject right = (DataObject) inner.next();
                if (matches(left, attrNames, right)) {
                    result.add(right);                               
                }
            }
        }
        results.push(result);
    }

    /** Empty Implemeantation to be overriden by Subclasses 
     *
     * @throws QueryException always since you should override it.
     */
    protected void computeIsSource() throws QueryException {
        throw new QueryException("ISSOURCE Query is not supported");
    }

    /** Empty Implemeantation to be overriden by Subclasses 
     *
     * @throws QueryException always since you should override it.
     */
    protected void computeIsDest() throws QueryException {
        throw new QueryException("ISDEST Query is not supported");
    }

    /** Generic method to visit some BinaryOperator (AND, OR, MATCHES).
     *<p>
     *  You propably wish to optimize this to use some operations on your actual
     *  realm. 
     *</p>
     */
    @Override
	public void visitNode(BinaryOper anOper) throws QueryException {

        anOper.getNode1().visitNode(this);
        anOper.getNode2().visitNode(this);
    
        switch (anOper.getKind()) {
            case QueryNode.AND:
                computeAnd();
                break;
            case QueryNode.OR:
                computeOr();
                break;
            case QueryNode.MATCHES:
                computeMatches();
                break;
            case QueryNode.ISDEST:
                computeIsDest();
                break;
            case QueryNode.ISSOURCE:
                computeIsSource();
                break;
            default:
                super.visitNode(anOper);
        }
    }
    
    /** 
     * Filter given Iterator by the given Filter.
     *
     * @param dest  Elements that did match the filter are added here.
     */
    protected void doFilter(Filter filter, Iterator iter, Set dest)  
        throws QueryException {
    
        while (iter.hasNext()) {
            Object theDO = iter.next();
            if (filter.accept(theDO)) {
                dest.add(theDO);
            }
        }
    }

    /** 
     * Filter allElements() by the given Filter.
     */
    protected Set doFilter(Filter filter)  
        throws QueryException {
    
        // assume a match Rate of 25 % ...
        Set       all    = previous != null ? previous : getAllElements();
                         // assume a match Rate of 25 % ...
        Set       result = createSet(all.size() >> 2);
        doFilter(filter, all.iterator(), result);
        return result;
    }

    /** 
     * Generic method to visit some BinaryFilters (=, &lt;, &gt;,&lt;=, &gt;=, c=, L.).
     *<p>
     *  You propably wish to optimize this to use some filter on your actual
     *  realm. Note that in this implementation AND and OR are the
     *  same. You may change this in case you dont have unsharp 
     *  relevance values.
     *</p>
     */
    @Override
	public void visitNode(BinaryFilter aFilter) throws QueryException {

        String attribute = ((LiteralNode) aFilter.getNode1()).getValue().toString();
        Object value     = ((LiteralNode) aFilter.getNode2()).getValue();
        
        Filter    filter = null;
        
        switch (aFilter.getKind()) {
            case BinaryFilter.EQUALS:
                filter = DOAttributeFilter.createEqualsFilter(attribute, value);
                break;
            case BinaryFilter.LT:
                filter = DOAttributeFilter.createLTFilter(attribute, (Comparable) value);
                break;
            case BinaryFilter.LE:
                filter = DOAttributeFilter.createLEFilter(attribute, (Comparable) value);
                break;
            case BinaryFilter.GT:
                filter = DOAttributeFilter.createGTFilter(attribute, (Comparable) value);
                break;
            case BinaryFilter.GE:
                filter = DOAttributeFilter.createGEFilter(attribute, (Comparable) value);
                break;
            case BinaryFilter.ISIN:
                filter = DOAttributeFilter.createContainsFilter(attribute, value.toString());
                break;
            case BinaryFilter.LIKE:
                try {
                    filter = new REFilter(attribute, value.toString());
                } catch (PatternSyntaxException psx) {
                    throw new QueryException(psx);
                }
                break;
            default:
                super.visitNode(aFilter);
        }
        if (filter != null) {
            results.push(filter);
        }
    }

    /** Main resolving Method, Resolve a query for given QueryNode.
     *
     * @return a Map Sorted by relevance.
     */
    public Set resolve() throws QueryException {
        
        rootNode.visitNode(this); 
        Object top = results.pop();
        Set result = null;
        if (top instanceof Set) {
            result = (Set) top;
        }
        else if (top instanceof Filter) {
            result = doFilter((Filter) top);
        }
        else
            throw new QueryException (
                "Unexpected Object on ResolverStack, "
              + "Looks like you should override resolve()");
                
        return result;
    }

    /** This comparator is needed to implement the ORDER BY Statement.
     * <p>
     *  In addition it cares for a stable sort order.
     *  You may override this Method in case your DataObjects may
     *  have some better/faster method than getIdentifier() to compare them.
     * </p>
     */
    @Override
	public int compare(Object o1, Object o2)  {
    
        int result = 0;
        if (orderBy != null) {
            result = orderBy.compare(o1, o2);
        }
        // Provide STABLE order even if without orderBy
        // KHA: Mhh, even the Hashcode of two none equal objects may be the same :-(
        if (result == 0 && o1 != o2) {
			TLID name1 = ((DataObject) o1).getIdentifier();
			TLID name2 = ((DataObject) o2).getIdentifier();
			result = KBUtils.compareIds(name1, name2);
        }
        return result;
    }
    
}
