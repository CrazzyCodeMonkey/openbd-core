package com.intridea.io.vfs.provider.s3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.util.UserAuthenticatorUtils;
import org.aw20.amazon.S3AwImpl;

/**
 * An S3 file provider. Create an S3 file system out of an S3 file name. Also
 * defines the capabilities of the file system.
 * 
 * @author Marat Komarov
 * @author Matthias L. Jugel
 * 
 * Modified by Alan Williamson @ July 2010
 * 
 */
public class S3FileProvider extends AbstractOriginatingFileProvider {

	public final static Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(
			Capability.CREATE, 
			Capability.DELETE,
			// Capability.RENAME,
			Capability.GET_TYPE, 
			Capability.GET_LAST_MODIFIED, 
			Capability.SET_LAST_MODIFIED_FILE, 
			Capability.SET_LAST_MODIFIED_FOLDER, 
			Capability.LIST_CHILDREN, 
			Capability.READ_CONTENT, 
			Capability.URI, Capability.WRITE_CONTENT
			));

	/**
	 * Auth data types necessary for AWS authentification.
	 */
	public final static UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] { UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD };

	/**
	 * Default options for S3 file system.
	 */
	private static FileSystemOptions defaultOptions = new FileSystemOptions();

	/**
	 * Returns default S3 file system options. Use it to set AWS auth credentials.
	 * 
	 * @return
	 */
	public static FileSystemOptions getDefaultFileSystemOptions() {
		return defaultOptions;
	}


	public S3FileProvider() {
		super();
		setFileNameParser(S3FileNameParser.getInstance());
	}

	/**
	 * Create a file system with the S3 root provided.
	 * 
	 * @param fileName
	 *          the S3 file name that defines the root (bucket)
	 * @param fileSystemOptions
	 *          file system options
	 * @return an S3 file system
	 * @throws FileSystemException
	 *           if the file system cannot be created
	 */
	protected FileSystem doCreateFileSystem(FileName fileName, FileSystemOptions fileSystemOptions) throws FileSystemException {
		FileSystemOptions fsOptions = fileSystemOptions != null ? fileSystemOptions : getDefaultFileSystemOptions();
		
		// Initialize S3 service.
		UserAuthenticationData authData = null;

		try {
			// Read authData from file system options
			authData = UserAuthenticatorUtils.authenticate(fsOptions, AUTHENTICATOR_TYPES);

			// Fetch AWS key-id and secret key from authData
			String keyId 	= UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME, null));
			String secret	= UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, null));
			String domain = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.DOMAIN, null));

			if (keyId.length() + secret.length() == 0) {
				throw new FileSystemException("Empty AWS credentials");
			}
			
			// Construct S3 file system
			return new S3FileSystem( (S3FileName)fileName, new S3AwImpl(keyId,secret,domain), fsOptions);
		} finally {
			UserAuthenticatorUtils.cleanup(authData);
		}

	}

	/**
	 * Get the capabilities of the file system provider.
	 * 
	 * @return the file system capabilities
	 */
	public Collection<Capability> getCapabilities() {
		return capabilities;
	}
}
