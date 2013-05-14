## The host to which email will be submitted.  This host must be able
#  to act as an SMTP relay for the CVS server.
SMTP_HOST = 'mx.sourceforge.net'

## The domain that will be tacked onto usernames as the reply-to
#  address for the emails.   Unfortunately, this locks everyone into
#  having to have at least an alias in this domain (as the users are
#  local to the cvs server).
#
#  This really should be fixed to support users across multiple
#  domains.  Maybe a dictionary in here that maps from user to email
#  address?
EMAIL_DOMAIN = 'sourceforge.net'
