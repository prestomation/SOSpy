# -*- encoding: utf-8 -*-
# This file is distributed under the same license as the Django package.
#

DATE_FORMAT = r'j \de F \de Y'
TIME_FORMAT = 'H:i:s'
DATETIME_FORMAT = r'j \de F \de Y \a \l\a\s H:i'
YEAR_MONTH_FORMAT = r'F \de Y'
MONTH_DAY_FORMAT = r'j \de F'
SHORT_DATE_FORMAT = 'd/m/Y'
SHORT_DATETIME_FORMAT = 'd/m/Y H:i'
FIRST_DAY_OF_WEEK = 1 			# Monday: ISO 8601 
DATE_INPUT_FORMATS = (
    '%d/%m/%Y', '%d/%m/%y',            	# '25/10/2006', '25/10/06'
    '%Y%m%d',                          	# '20061025'

)
TIME_INPUT_FORMATS = (
    '%H:%M:%S', '%H:%M',		# '14:30:59', '14:30'
)
DATETIME_INPUT_FORMATS = (
    '%d/%m/%Y %H:%M:%S',
    '%d/%m/%Y %H:%M',
    '%d/%m/%y %H:%M:%S',
    '%d/%m/%y %H:%M',
)
DECIMAL_SEPARATOR = '.' 		# ',' is also official (less common): NOM-008-SCFI-2002
THOUSAND_SEPARATOR = ' '		# white space
NUMBER_GROUPING = 3

