/** Copyright 2013,2014 hbz
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.metafacture.biblio;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An Aleph-MAB-XML reader.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description ( "A MAB XML reader" )
@In ( XmlReceiver.class )
@Out ( StreamReceiver.class )
@FluxCommand ( "handle-mabxml" )
public final class AlephMabXmlHandler extends DefaultXmlPipe<StreamReceiver> {

    private static final String SUBFIELD="subfield";
    private static final String DATAFIELD="datafield";
    private static final String CONTROLLFIELD="controlfield";
    private static final String RECORD="ListRecords";
    private static final String LEADER="leader";
    private static final String DATAFIELD_ATTRIBUTE="tag";
    private static final String SUBFIELD_ATTRIBUTE="code";
    private static final String INDICATOR1="ind1";
    private static final String INDICATOR2="ind2";
    private String currentTag="";
    private StringBuilder builder=new StringBuilder();
    private String alephid;
    private String id001;
    private String tagIndi12;
    private String headerstatus;

    @Override
    public void characters (
            final char[] chars ,
            final int start ,
            final int length )
            throws SAXException {
        this.builder.append(
                chars,
                start,
                length);
    }

    @Override
    public void endElement (
            final String uri ,
            final String localName ,
            final String qName )
            throws SAXException {
        if(AlephMabXmlHandler.CONTROLLFIELD.equals(
                localName)){
            getReceiver().literal(
                    this.currentTag,
                    this.builder.toString().trim());
            getReceiver().endEntity();
        }else if(AlephMabXmlHandler.SUBFIELD.equals(
                localName)){
            getReceiver().literal(
                    this.currentTag,
                    this.builder.toString().trim());
            if(tagIndi12.equals(
                    "001-1")){
                id001=this.builder.toString()
                        .trim();
            }
        }else if(AlephMabXmlHandler.DATAFIELD.equals(
                localName)){
            getReceiver().endEntity();
        }else if(AlephMabXmlHandler.RECORD.equals(
                localName)){
            System.out.println(
                    "alephid001:"+alephid+","+id001+","+(headerstatus!=null
                            ?headerstatus:""));
            getReceiver().endRecord();
        }else if("header".equals(
                localName)){
            alephid=builder.toString().trim().replaceAll(
                    ".*aleph-publish:(\\d+).*",
                    "$1");
        }
    }

    @Override
    public void startElement (
            final String uri ,
            final String localName ,
            final String qName ,
            final Attributes attributes ) throws SAXException {
        if(AlephMabXmlHandler.CONTROLLFIELD.equals(
                localName)){
            this.builder=new StringBuilder();
            this.currentTag="";
            getReceiver().startEntity(
                    attributes.getValue(
                            AlephMabXmlHandler.DATAFIELD_ATTRIBUTE));
        }else if(AlephMabXmlHandler.SUBFIELD.equals(
                localName)){
            this.builder=new StringBuilder();
            this.currentTag=attributes.getValue(
                    AlephMabXmlHandler.SUBFIELD_ATTRIBUTE);
        }else if(AlephMabXmlHandler.DATAFIELD.equals(
                localName)){
            tagIndi12=attributes.getValue(
                    AlephMabXmlHandler.DATAFIELD_ATTRIBUTE)
                    +attributes.getValue(
                            AlephMabXmlHandler.INDICATOR1)
                    +attributes.getValue(
                            AlephMabXmlHandler.INDICATOR2);
            getReceiver().startEntity(
                    tagIndi12);

        }else if(AlephMabXmlHandler.RECORD.equals(
                localName)){
            this.builder=new StringBuilder();
            getReceiver().startRecord(
                    "");
            tagIndi12=null;
            alephid=null;
            id001=null;
            headerstatus=null;
        }else if(AlephMabXmlHandler.LEADER.equals(
                localName)){
            this.builder=new StringBuilder();
            this.currentTag=AlephMabXmlHandler.LEADER;
        }else if("header".equals(
                localName)){
            if(attributes.getLength()>0){
                headerstatus=attributes.getValue(
                        0);
            }
        }
    }

}
