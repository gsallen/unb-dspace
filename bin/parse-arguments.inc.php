<?php
/** 
 * Parse command-line arguments of the '--argname value' or 
 * '--arg_no_value' variety
 *
 * Shared by define-structure & map-handles. To be replaced when
 * the PHP hacks are made more DSpace-friendly with Java classes
 * similar to existing admin tools.
 */
function parse_arguments($args) {
  array_shift($args);

  $arg_index = 0;
  $parsed_args = array();
  
  while ($arg_index < sizeof($args)) {
    // We expect the next value in the list to be an argument name
    if (strpos($args[$arg_index], ARG_PREFIX) === FALSE) {
      echo usage();
      exit(E_USER_ERROR);
    }

    // Strip prefix & keep current argument as a key
    $key = substr_replace($args[$arg_index], '', 0, strlen(ARG_PREFIX));
    
    // What if the key already exists?
    if (array_key_exists($key, $parsed_args)) {
      echo usage();
      exit(E_USER_ERROR);
    }
    
    // Nah, it's fine:
    $parsed_args[$key] = NULL;
    
    // Increment the index & exit if we're at the end of the line
    $arg_index++;
    if ($arg_index >= sizeof($args)) {
      break;
    }
    
    if (strpos($args[$arg_index], ARG_PREFIX) === FALSE) {
      // Assume it's a value for the current arg: store & increment
      $parsed_args[$key] = $args[$arg_index++];
    }
    // Otherwise, let the next iteration handle the next named arg.
  }
  return $parsed_args;
}
?>