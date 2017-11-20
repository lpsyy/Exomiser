INSERT INTO regulatory_regions SELECT *
                               FROM CSVREAD('${import.path}/fantom.pg', 'chromosome|start|end|feature_type',
                                            'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
